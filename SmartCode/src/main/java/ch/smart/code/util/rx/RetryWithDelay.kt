package ch.smart.code.util.rx

import io.reactivex.Observable
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Function
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 类描述：RxJava 重试
 * maxRetries       最大重试次数
 * retryDelayMillis 重试间隔时间，毫秒
 * 创建人：chenhao
 * 创建时间：2016/11/12 11:05
 * 修改人：chenhao
 * 修改时间：2016/11/12 11:05
 * 修改备注：
 *
 */
class RetryWithDelay(private val maxRetries: Int, private val retryDelayMillis: Int) :
    Function<Observable<out Throwable>, Observable<*>> {
    private var retryCount: Int = 0


    override fun apply(@NonNull attempts: Observable<out Throwable>): Observable<*> {
        return attempts.flatMap(Function<Throwable, Observable<*>> { throwable ->
            if (++retryCount <= maxRetries) {
                // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
                Timber.e(
                    "get error, it will try after %s millisecond, retry count %s",
                    retryDelayMillis,
                    retryCount
                )
                return@Function Observable.timer(retryDelayMillis.toLong(), TimeUnit.MILLISECONDS)
            }
            // Max retries hit. Just pass the error along.
            Observable.error<Any>(throwable)
        })
    }
}
