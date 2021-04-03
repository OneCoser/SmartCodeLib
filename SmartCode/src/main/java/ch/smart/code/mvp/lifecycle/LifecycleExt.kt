package ch.smart.code.mvp.lifecycle

import android.app.Activity
import androidx.fragment.app.Fragment
import ch.smart.code.util.rx.toIoAndMain
import ch.smart.code.mvp.IView
import com.trello.rxlifecycle3.LifecycleTransformer
import com.trello.rxlifecycle3.RxLifecycle
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.android.FragmentEvent
import io.reactivex.Observable
import io.reactivex.Single

/**
 * 类描述：生命周期绑定操作
 */

fun Activity?.lifecycleSubject(event: ActivityEvent) {
    if (this is ActivityLifecycleable) {
        this.provideLifecycleSubject().onNext(event)
    }
}

fun Fragment?.lifecycleSubject(event: FragmentEvent) {
    if (this is FragmentLifecycleable) {
        this.provideLifecycleSubject().onNext(event)
    }
}

fun <T> Observable<T>.applySchedulers(view: IView?): Observable<T> {
    if (view == null) return Observable.empty()
    return this.toIoAndMain()
        .doOnSubscribe {
            view.showLoading() //显示进度条
        }
        .doFinally {
            view.hideLoading() //隐藏进度条
        }.bindObservableToDestroyV(view)
}

fun <T> Observable<T>.bindObservableToDestroyV(view: IView?): Observable<T> {
    val lf = view.bindViewLifecycleToDestroy<T>() ?: return Observable.empty()
    return this.compose(lf)
}

fun <T> Observable<T>.bindObservableToDestroyL(life: Lifecycleable<*>?): Observable<T> {
    val lf = life.bindLifecycleableToDestroy<T>() ?: return Observable.empty()
    return this.compose(lf)
}

fun <T> Single<T>.bindSingleToDestroyV(view: IView?): Single<T> {
    val lf = view.bindViewLifecycleToDestroy<T>() ?: return Single.error(Exception("view is null"))
    return this.compose(lf)
}

fun <T> Single<T>.bindSingleToDestroyL(life: Lifecycleable<*>?): Single<T> {
    val lf = life.bindLifecycleableToDestroy<T>()
        ?: return Single.error(Exception("Lifecycleable<*> is null"))
    return this.compose(lf)
}

fun <T> IView?.bindViewLifecycleToDestroy(): LifecycleTransformer<T>? {
    return when (this) {
        is ActivityLifecycleable -> {
            RxLifecycle.bindUntilEvent(this.provideLifecycleSubject(), ActivityEvent.DESTROY)
        }
        is FragmentLifecycleable -> {
            RxLifecycle.bindUntilEvent(this.provideLifecycleSubject(), FragmentEvent.DESTROY_VIEW)
        }
        else -> null
    }
}

fun <T> Lifecycleable<*>?.bindLifecycleableToDestroy(): LifecycleTransformer<T>? {
    return when (this) {
        is ActivityLifecycleable -> {
            RxLifecycle.bindUntilEvent(this.provideLifecycleSubject(), ActivityEvent.DESTROY)
        }
        is FragmentLifecycleable -> {
            RxLifecycle.bindUntilEvent(this.provideLifecycleSubject(), FragmentEvent.DESTROY_VIEW)
        }
        else -> null
    }
}