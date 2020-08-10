package ch.smart.code.network

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.CopyOnWriteArraySet

abstract class HttpObserver<T>(protected var showMsg: Boolean = true) : Observer<T> {

    override fun onSubscribe(d: Disposable) {}

    override fun onError(throwable: Throwable) {
        errorHandles.forEach {
            if (it.errorHandle(throwable, showMsg)) {
                return
            }
        }
        DEFAULT_ERROR_HANDLE.errorHandle(throwable, showMsg)
    }

    override fun onComplete() {}

    companion object {
        private val DEFAULT_ERROR_HANDLE by lazy { DefaultErrorHandle() }
        private val errorHandles: MutableSet<ResponseErrorHandle> = CopyOnWriteArraySet()
        fun addErrorHandle(errorHandle: ResponseErrorHandle) {
            errorHandles.add(errorHandle)
        }
    }
}