package ch.smart.code.util.rx

import io.reactivex.FlowableSubscriber
import org.reactivestreams.Subscription
import timber.log.Timber

abstract class SimpleFlowableSubscriber<T> : FlowableSubscriber<T> {
    override fun onComplete() {
    }

    override fun onSubscribe(s: Subscription) {
        s.request(Long.MAX_VALUE)
    }

    override fun onError(t: Throwable?) {
        Timber.e(t)
    }


}