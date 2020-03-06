package com.wjx.android.wanandroidmvvm.ui.search

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.wjx.android.wanandroidmvvm.R
import com.wjx.android.wanandroidmvvm.base.BaseArticle.BaseArticleListActivity
import com.wjx.android.wanandroidmvvm.base.BaseArticle.data.Article
import com.wjx.android.wanandroidmvvm.base.utils.Util
import com.wjx.android.wanandroidmvvm.base.utils.Util.hideKeyboard
import com.wjx.android.wanandroidmvvm.ui.search.data.HotKeyResponse
import com.wjx.android.wanandroidmvvm.ui.search.viewmodel.SearchViewModel
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.custom_search.*
import kotlinx.android.synthetic.main.custom_search.view.*
import kotlinx.android.synthetic.main.history_foot.view.*
import org.jetbrains.anko.toast

class SearchActivity : BaseArticleListActivity<SearchViewModel>() {
    private var mCurrentPageNum: Int = 0

    private var mMaxHistory = 10

    private var mHistoryIndex: Int = 0

    private var isShow = true

    private lateinit var mHistoryFootView: View

    private val mSearchHistoryAdapter by lazy { SearchHistoryAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_search

    override fun initView() {
        super.initView()
        initSearch()
        initHistory()
        search_back.setOnClickListener{
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun showDestroyReveal(): Boolean = true

    override fun initData() {
        mViewModel.loadSearchHistory()
        mViewModel.loadHotkey()
    }

    override fun initDataObserver() {
        super.initDataObserver()
        mViewModel.mHotKeyData.observe(this, Observer { resonse ->
            resonse?.let {
                showHotKeyTags(it.data)
            }
        })
        mViewModel.mSearResultData.observe(this, Observer { response ->
            response?.let {
                showSearchResultList(it.data.datas)
                if (!search_input.text.toString().isEmpty()) {
                    mViewModel.addSearchHistory(search_input.text.toString())
                }
            }
        })
        mViewModel.mDeleteHistory.observe(this, Observer {
            mSearchHistoryAdapter.remove(mHistoryIndex)
            if (mSearchHistoryAdapter.data.isEmpty()) {
                mHistoryFootView.visibility = View.GONE
            }
        })

        mViewModel.mAddSearchHistory.observe(this, Observer {
            updateRecordPosition(search_input.text.toString())
        })
        mViewModel.mSearchHistory.observe(this, Observer { history ->
            var historyNames = history?.map { it.name }?.toList()
            historyNames?.let {
                if (it.isEmpty()) {
                    mHistoryFootView.visibility = View.GONE
                } else {
                    mSearchHistoryAdapter.addData(it)
                }
            }
        })

        mViewModel.mClearHistory.observe(this, Observer {
            mSearchHistoryAdapter.setNewData(null)
            mHistoryFootView.visibility = View.GONE
        })
    }

    private fun initHistory() {
        search_history.layoutManager = LinearLayoutManager(this)
        search_history.adapter = mSearchHistoryAdapter
        search_history.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        mHistoryFootView =
            LayoutInflater.from(this).inflate(R.layout.history_foot, activity_search, false)
        mSearchHistoryAdapter.setFooterView(mHistoryFootView)
        mHistoryFootView.history_clear.setOnClickListener {
            mViewModel.clearSearchHistory()
        }
        mSearchHistoryAdapter.setOnItemChildClickListener { _, view, position ->
            if (view.id == R.id.history_delete) {
                mHistoryIndex = position
                mViewModel.deleteSearchHistory(mSearchHistoryAdapter.data[position])
            }
        }
        mSearchHistoryAdapter.setOnItemClickListener { _, _, position ->
            initSearchKey(mSearchHistoryAdapter.data[position])
        }
    }

    override fun onLoadMoreData() {
       mViewModel.loadSearchResult(++mCurrentPageNum, search_input.text.toString())
    }

    override fun onRefreshData() {}

    private fun initSearch() {
        search_back.setOnClickListener { finish() }
        search_close.setOnClickListener {
            search_bar.search_input.setText("")
            displaySearchView()
            hideKeyboard()
        }
        search_button.setOnClickListener { view ->
            initSearchKey(search_input.text.toString())
        }
        search_input.setOnEditorActionListener(TextView.OnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                initSearchKey(search_input.text.toString())
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun initSearchKey(key: String) {
        if (key.isEmpty()) {
            displaySearchView()
            toast("请输入关键词")
            return
        }
        hideKeyboard()
        search_input.setText(key)
        search_input.setSelection(key.length)
        mViewModel.loadSearchResult(mCurrentPageNum, key)
    }

    private fun displaySearchView() {
        if (isShow) {
            return
        }
        search_text_top.visibility = View.VISIBLE
        search_flowlayout.visibility = View.VISIBLE
        search_text_history.visibility = View.VISIBLE
        search_history.visibility = View.VISIBLE
        mAdapter.setNewData(null)
        isShow = true
    }

    private fun hideSearchView() {
        if (!isShow) return
        search_text_top.visibility = View.GONE
        search_flowlayout.visibility = View.GONE
        search_text_history.visibility = View.GONE
        search_history.visibility = View.GONE
        isShow = false
    }


    private fun showHotKeyTags(hotkeyList: List<HotKeyResponse>) {
        val tags = hotkeyList.map { it.name }.toList()
        search_flowlayout.adapter = object : TagAdapter<String>(tags) {
            override fun getView(parent: FlowLayout?, position: Int, t: String?): View {
                val tagText =  LayoutInflater.from(this@SearchActivity)
                    .inflate(R.layout.flow_layout, parent, false) as TextView
                tagText.setText(tags[position])
                tagText.background
                    .setColorFilter(Util.randomColor(), PorterDuff.Mode.SRC_ATOP)
                tagText.setTextColor(getColor(R.color.white))
                return tagText
            }
        }
        search_flowlayout.setOnTagClickListener { view, position, _ ->
            initSearchKey(tags[position])
            true
        }
    }

    private fun showSearchResultList(searchResultList: List<Article>) {
        addData(searchResultList)
        hideSearchView()
    }

    private fun updateRecordPosition(name: String) {

        val records = mSearchHistoryAdapter.data

        // 判断是否存在一个同样的搜索记录
        val index = records.indexOf(name)
        if (index == -1) {

            if (records.size >= mMaxHistory) {
                // 删除最后一条
                mSearchHistoryAdapter.remove(mMaxHistory - 1)
            }

            // 不存在就添加
            mSearchHistoryAdapter.addData(0, name)
            return
        }

        if (index != 0) {
            // 存在就调整该记录到第一条。
            mSearchHistoryAdapter.remove(index)
            mSearchHistoryAdapter.addData(0, name)
        }
    }
}