package ch.smart.code.util.rx

import io.reactivex.observers.DefaultObserver
import timber.log.Timber

abstract class SimpleObserver<T> : DefaultObserver<T>() {
    override fun onComplete() {
    }

    override fun onError(e: Throwable) {
        Timber.e(e)
    }

    fun dispose() {
        cancel()
    }
}
