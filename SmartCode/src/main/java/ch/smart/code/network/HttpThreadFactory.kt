package ch.smart.code.network

import timber.log.Timber
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 类描述：Http线程构建
 */
class HttpThreadFactory(prefix: String, private val priority: Int) : ThreadFactory {

    companion object {
        @JvmStatic
        private val POOL_NUMBER = AtomicInteger(1)
    }

    private val count = AtomicLong()

    private val namePrefix: String = prefix + "-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-"

    override fun newThread(r: Runnable?): Thread {
        val t = object : Thread(r, namePrefix + count.andIncrement) {
            override fun run() {
                try {
                    super.run()
                } catch (ignore: Throwable) {
                    Timber.e(ignore, "Request threw uncaught throwable")
                }
            }
        }
        if (t.isDaemon) {
            t.isDaemon = false
        }
        t.priority = priority
        return t
    }
}