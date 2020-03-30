package com.wjx.android.wanandroidmvvm.ui.rank.view

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.wjx.android.wanandroidmvvm.R
import com.wjx.android.wanandroidmvvm.base.BaseArticle.data.Article
import com.wjx.android.wanandroidmvvm.base.BaseLifeCycleActivity
import com.wjx.android.wanandroidmvvm.base.utils.ChangeThemeEvent
import com.wjx.android.wanandroidmvvm.base.utils.Util
import com.wjx.android.wanandroidmvvm.ui.activity.ArticleDetailActivity
import com.wjx.android.wanandroidmvvm.ui.rank.adapter.RankAdapter
import com.wjx.android.wanandroidmvvm.ui.rank.data.IntegralResponse
import com.wjx.android.wanandroidmvvm.ui.rank.viewmodel.RankViewModel
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.android.synthetic.main.custom_bar.view.*
import kotlinx.android.synthetic.main.fragment_article_list.*
import kotlinx.android.synthetic.main.fragment_article_list.mRvArticle
import kotlinx.android.synthetic.main.fragment_article_list.mSrlRefresh
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.startActivity

class RankActivity : BaseLifeCycleActivity<RankViewModel>() {
    private var mCurrentPage: Int = 1
    private lateinit var headerView: View
    private lateinit var mAdapter: RankAdapter
    override fun getLayoutId(): Int = R.layout.activity_rank

    override fun initView() {
        super.initView()
        mAdapter = RankAdapter(R.layout.rank_item, null)
        initHeaderView()
        initRefresh()
        mRvArticle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRvArticle.adapter = mAdapter
        mAdapter.setEnableLoadMore(true)
        mAdapter.setOnLoadMoreListener({ onLoadMoreData() }, mRvArticle)
    }

    override fun initData() {
        super.initData()
        mCurrentPage = 1
        mViewModel.loadRankList(mCurrentPage)
        mViewModel.loadMeRankInfo()
    }

    override fun initDataObserver() {
        mViewModel.mRankListData.observe(this, Observer { response ->
            response.let {
                addData(it.data.datas)
            }
        })
        mViewModel.mMeRankInfo.observe(this, Observer { response ->
            response.let {
                integral_melevel.text = "等级：" + it.data.level.toString()
                integral_mename.text = "用户：" + it.data.username
                integral_mecount.text = "积分：" + it.data.coinCount.toString()
            }
        })
    }

    fun onRefreshData() {
        mCurrentPage = 1
        mViewModel.loadRankList(mCurrentPage)
        mViewModel.loadMeRankInfo()
    }

    fun onLoadMoreData() {
        mViewModel.loadRankList(++mCurrentPage)
    }

    private fun initHeaderView() {
        headerView = View.inflate(this, R.layout.custom_bar, null)
        headerView.detail_title.text = "积分排行"
        headerView.detail_back.visibility = View.VISIBLE
        headerView.detail_search.visibility = View.VISIBLE
        headerView.detail_search.setImageResource(R.drawable.ic_help)
        headerView.addition_menu.visibility = View.VISIBLE
        headerView.detail_search.setOnClickListener { onRulePressed() }
        headerView.addition_menu.setOnClickListener { onHistoryPressed() }
        headerView.detail_back.setOnClickListener { onBackPressed() }
        mAdapter.addHeaderView(headerView)
        initColor()
    }

    private fun initColor() {
        headerView.setBackgroundColor(Util.getColor(this))
        integral_mecard.setCardBackgroundColor(Util.getColor(this))
    }

    private fun initRefresh() {
        // 设置下拉刷新的loading颜色
        mSrlRefresh.setProgressBackgroundColorSchemeColor(Util.getColor(this))
        mSrlRefresh.setColorSchemeColors(Color.WHITE)
        mSrlRefresh.setOnRefreshListener { onRefreshData() }
    }

    fun addData(integralList: List<IntegralResponse>) {

        // 返回列表为空显示加载完毕
        if (integralList.isEmpty()) {
            mAdapter.loadMoreEnd()
            return
        }

        // 如果是下拉刷新状态，直接设置数据
        if (mSrlRefresh.isRefreshing) {
            mSrlRefresh.isRefreshing = false
            mAdapter.setNewData(integralList)
            mAdapter.loadMoreComplete()
            return
        }

        // 初始化状态直接加载数据
        mAdapter.addData(integralList)
        mAdapter.loadMoreComplete()
    }

    override fun showDestroyReveal(): Boolean = true
    override fun onBackPressed() = finish()

    private fun onRulePressed() {
        val intent: Intent = Intent(this, ArticleDetailActivity::class.java)
        intent.putExtra("url", "https://www.wanandroid.com/blog/show/2653")
        intent.putExtra("title", getString(R.string.rank_rule))
        startActivity(intent)
    }

    private fun onHistoryPressed() {
        startActivity<IntegralHistoryActivity>()
    }

    @Subscribe
    fun settingEvent(event: ChangeThemeEvent) {
        initColor()
        mSrlRefresh.setProgressBackgroundColorSchemeColor(Util.getColor(this))
    }

}