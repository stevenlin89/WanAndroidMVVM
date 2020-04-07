package com.wjx.android.wanandroidmvvm.ui.common.repository

import androidx.lifecycle.MutableLiveData
import com.wjx.android.wanandroidmvvm.base.observer.BaseObserver
import com.wjx.android.wanandroidmvvm.network.response.BaseResponse
import com.wjx.android.wanandroidmvvm.network.response.EmptyResponse
import com.wjx.android.wanandroidmvvm.base.repository.ApiRepository
import com.wjx.android.wanandroidmvvm.common.state.State
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @date: 2020/02/25
 * Time: 20:40
 */
abstract class ArticleRepository(val loadState: MutableLiveData<State>) : ApiRepository() {

    fun collect(id: Int, liveData: MutableLiveData<BaseResponse<EmptyResponse>>) {
        apiService.collect(id).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                BaseObserver(
                    liveData,
                    loadState,
                    this
                )
            )
    }

    fun unCollect(id: Int, liveData: MutableLiveData<BaseResponse<EmptyResponse>>) {
        apiService.unCollect(id).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                BaseObserver(
                    liveData,
                    loadState,
                    this
                )
            )
    }
}