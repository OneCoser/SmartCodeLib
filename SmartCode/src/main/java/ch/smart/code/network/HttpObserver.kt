package ch.smart.code.network

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 类描述：接口请求响应
 */
abstract class HttpObserver<T>(protected var showMsg: Boolean = true) : Observer<T> {
    private var disposable: Disposable? = null

    override fun onSubscribe(d: Disposable) {
        disposable = d
    }

    override fun onError(throwable: Throwable) {
        errorHandles.forEach {
            if (it.errorHandle(throwable, showMsg)) {
                return
            }
        }
        DEFAULT_ERROR_HANDLE.errorHandle(throwable, showMsg)
    }

    override fun onComplete() {
        disposable()
    }

    fun disposable() {
        disposable?.dispose()
        disposable = null
    }

    companion object {
        private val DEFAULT_ERROR_HANDLE by lazy { DefaultErrorHandle() }
        private val errorHandles: MutableSet<ResponseErrorHandle> = CopyOnWriteArraySet()
        fun addErrorHandle(errorHandle: ResponseErrorHandle) {
            errorHandles.add(errorHandle)
        }
    }
}
