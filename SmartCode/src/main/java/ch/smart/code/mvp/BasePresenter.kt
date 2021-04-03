package ch.smart.code.mvp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.simple.eventbus.EventBus

/**
 * 项目名称：Consignor
 * 类描述：
 * 创建人：chenhao
 * 创建时间：3/11/21 7:34 PM
 * 修改人：chenhao
 * 修改时间：3/11/21 7:34 PM
 * 修改备注：
 * @version
 */
open class BasePresenter<M : IModel, V : IView>(
    protected var model: M? = null,
    protected var rootView: V? = null
) : IPresenter, LifecycleObserver {


    protected val compositeDisposable by lazy {
        CompositeDisposable()
    }

    init {
        onStart()
    }

    open fun useEventBus(): Boolean {
        return true
    }

    open fun addDispose(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    open fun unDispose() {
        compositeDisposable.clear()
    }

    override fun onStart() {
        (rootView as? LifecycleOwner)?.let { owner ->
            owner.lifecycle.addObserver(this)
            (model as? LifecycleObserver)?.let { mObs ->
                owner.lifecycle.addObserver(mObs)
            }
        }
        if (useEventBus()) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        if (useEventBus()) {
            EventBus.getDefault().unregister(this)
        }
        unDispose()
        model?.onDestroy()
        model = null
        rootView = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy(owner: LifecycleOwner) {
        /**
         * 注意, 如果在这里调用了 [.onDestroy] 方法, 会出现某些地方引用 `mModel` 或 `mRootView` 为 null 的情况
         * 比如在 [RxLifecycle] 终止 [Observable] 时, 在 [io.reactivex.Observable.doFinally] 中却引用了 `mRootView` 做一些释放资源的操作, 此时会空指针
         * 或者如果你声明了多个 @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY) 时在其他 @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
         * 中引用了 `mModel` 或 `mRootView` 也可能会出现此情况
         */
        owner.lifecycle.removeObserver(this)
    }
}