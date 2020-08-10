package ch.smart.code.util.rx


import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.math.pow


class TimeoutRetryWithDelay : Function<Observable<out Throwable>, Observable<*>> {

    private val mMaxRetries: Int//最大重试次数


    constructor() {
        mMaxRetries = 3
    }


    /**
     * @param maxRetries 最大重试次数
     */
    constructor(maxRetries: Int) {
        this.mMaxRetries = maxRetries
    }


    override fun apply(observable: Observable<out Throwable>): Observable<*> {
        return observable.zipWith(
            Observable.range(1, mMaxRetries + 1),
            BiFunction<Throwable, Int, Any> { throwable, retryCount ->
                //只有SocketTimeoutException才进行重试，如果是服务器返回错误不进行重试
                if (retryCount <= mMaxRetries && (throwable is SocketTimeoutException || throwable is ConnectException)) {
                    Timber.e(throwable)
                    retryCount
                } else {
                    throwable
                }
            }
        ).flatMap { o ->
            if (o is Throwable) {
                Observable.error<Any>(o)
            } else {
                //重试maxRetries次,并且每一次的重试时间都是15 ^ retryCount,指数退避算法
                val retryCount = o as Int
                val delay = 15.0.pow(retryCount.toDouble()).toLong()
                Timber.e(
                    "get error, it will try after %s millisecond, retry count %s",
                    delay,
                    retryCount
                )
                Observable.timer(delay, TimeUnit.MILLISECONDS)
            }
        }
    }
}
