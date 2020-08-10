package ch.smart.code.util.rx

import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable
import timber.log.Timber

abstract class SimpleMaybeObserver<T> : MaybeObserver<T> {
    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onError(e: Throwable) {
        Timber.e(e)
    }
}