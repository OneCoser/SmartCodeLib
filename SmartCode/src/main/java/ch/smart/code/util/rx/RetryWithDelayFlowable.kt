package ch.smart.code.util.rx

import io.reactivex.Flowable
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Function
import org.reactivestreams.Publisher
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 类描述：RxJava 重试
 * maxRetries       最大重试次数
 * retryDelayMillis 重试间隔时间，毫秒
 * 创建人：chenhao
 * 创建时间：2018/7/30 11:05
 * 修改人：
 * 修改时间：
 * 修改备注：
 */
class RetryWithDelayFlowable(private val maxRetries: Int, private val retryDelayMillis: Long) :
    Function<Flowable<out Throwable>, Publisher<*>> {

    private var retryCount: Int = 0

    override fun apply(@NonNull attempts: Flowable<out Throwable>): Flowable<*> {
        return attempts.flatMap { throwable ->
            if (++retryCount <= maxRetries) {
                // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
                Timber.e(
                    throwable,
                    "get error, it will try after %s millisecond, retry count %s",
                    retryDelayMillis,
                    retryCount
                )
                Flowable.timer(retryDelayMillis, TimeUnit.MILLISECONDS)
            } else {
                // Max retries hit. Just pass the error along.
                Flowable.error<Any>(throwable)
            }
        }
    }
}
