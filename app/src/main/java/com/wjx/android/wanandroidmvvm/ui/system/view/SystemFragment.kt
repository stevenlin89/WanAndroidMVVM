package com.wjx.android.wanandroidmvvm.ui.system.view

import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.wjx.android.wanandroidmvvm.R
import com.wjx.android.wanandroidmvvm.base.BaseLifeCycleFragment
import com.wjx.android.wanandroidmvvm.ui.system.adapter.SystemAdapter
import com.wjx.android.wanandroidmvvm.ui.system.data.SystemLabelResponse
import com.wjx.android.wanandroidmvvm.ui.system.data.SystemTabNameResponse
import com.wjx.android.wanandroidmvvm.ui.system.viewmodel.SystemViewModel
import kotlinx.android.synthetic.main.layout_system.*

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @date: 2020/02/27
 * Time: 17:01
 */
class SystemFragment : BaseLifeCycleFragment<SystemViewModel>() {
    protected lateinit var mAdapter: SystemAdapter

    private val fragments by lazy { arrayListOf<Fragment>() }

    override fun initDataObserver() {
        mViewModel.mSystemTabNameData.observe(this, Observer { response ->
            response?.let {
                setSystemTabData(it.data)
            }
        })
    }

    override fun initData() {
        mViewModel.loadSystemTab()
    }

    override fun getLayoutId(): Int = R.layout.layout_system

    override fun initView() {
        super.initView()
        initRefresh()
        initStatusColor()
        recycler_view?.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        mAdapter = SystemAdapter(R.layout.system_item, null)
        recycler_view.adapter = mAdapter
        mAdapter.setOnItemChildClickListener { _, _, position ->
            val item = mAdapter.getItem(position)
            item?.let {
            }
        }
    }

    private fun initStatusColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity!!.window.statusBarColor = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
        }
        if (ColorUtils.calculateLuminance(Color.TRANSPARENT) >= 0.5) { // 设置状态栏中字体的颜色为黑色
            activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else { // 跟随系统
            activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        initStatusColor()
    }

    private fun initRefresh() {
        // 设置下拉刷新的loading颜色
        system_refresh.setColorSchemeResources(R.color.colorPrimary)
        system_refresh.setOnRefreshListener { onRefreshData() }
    }

    private fun onRefreshData() {
        mViewModel.loadSystemTab()
    }

    private fun setSystemTabData(systemListName : List<SystemTabNameResponse>) {
        val chileItems = arrayListOf<SystemLabelResponse>()
        // 返回列表为空显示加载完毕
        if (systemListName.isEmpty()) {
            mAdapter.loadMoreEnd()
            return
        }

        // 如果是下拉刷新状态，直接设置数据
        if (system_refresh.isRefreshing) {
            system_refresh.isRefreshing = false
            mAdapter.setNewData(systemListName)
            mAdapter.loadMoreComplete()
            return
        }

        // 初始化状态直接加载数据
        mAdapter.addData(systemListName)
        mAdapter.loadMoreComplete()
    }
}