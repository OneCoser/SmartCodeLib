package ch.smart.code.util.rx

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * 项目名称：DSUtil
 * 类描述：倒计时工具类
 * 创建人：chenhao
 * 创建时间：2019-09-24 18:44
 * 修改人：chenhao
 * 修改时间：2019-09-24 18:44
 * 修改备注：
 * @version
 */
object RxCountDown {

    /**
     * 倒计时，单位秒
     */
    @JvmStatic
    fun countdown(time: Int): Observable<Int> {
        val countTime = if (time < 0) 0 else time
        return Observable.interval(0, 1, TimeUnit.SECONDS)
            .map { increaseTime -> countTime - increaseTime.toInt() }
            .take((countTime + 1).toLong())
            .observeOnMain()
    }

    /**
     * 倒计时，单位毫秒
     */
    @JvmStatic
    fun countdownToMilliseconds(time: Int): Observable<Int> {
        val countTime = if (time < 0) 0 else time
        return Observable.interval(1000, TimeUnit.MILLISECONDS)
            .map { increaseTime -> countTime - increaseTime.toInt() * 1000 }
            .take((countTime / 1000 + 1).toLong())
            .observeOnMain()
    }

}