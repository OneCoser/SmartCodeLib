package ch.smart.code.util.rx

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * 项目名称：Platformer
 * 类描述：
 * 创建人：chenhao
 * 创建时间：2017/9/28 11:58
 * 修改人：chenhao
 * 修改时间：2017/9/28 11:58
 * 修改备注：
 * @version
 */

fun <T> Single<T>.observeOnIo(): Single<T> {
    return this.observeOn(Schedulers.io())
}

fun <T> Single<T>.observeOnSingle(): Single<T> {
    return this.observeOn(Schedulers.single())
}

fun <T> Single<T>.observeOnMain(): Single<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

fun <T> Single<T>.observeOnNewThread(): Single<T> {
    return this.observeOn(Schedulers.newThread())
}

fun <T> Single<T>.observeOnTrampoline(): Single<T> {
    return this.observeOn(Schedulers.trampoline())
}

fun <T> Single<T>.observeOnComputation(): Single<T> {
    return this.observeOn(Schedulers.computation())
}

fun <T> Observable<T>.observeOnIo(): Observable<T> {
    return this.observeOn(Schedulers.io())
}

fun <T> Observable<T>.observeOnSingle(): Observable<T> {
    return this.observeOn(Schedulers.single())
}

fun <T> Observable<T>.observeOnMain(): Observable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.observeOnNewThread(): Observable<T> {
    return this.observeOn(Schedulers.newThread())
}

fun <T> Observable<T>.observeOnTrampoline(): Observable<T> {
    return this.observeOn(Schedulers.trampoline())
}

fun <T> Observable<T>.observeOnComputation(): Observable<T> {
    return this.observeOn(Schedulers.computation())
}

fun <T> Flowable<T>.observeOnIo(): Flowable<T> {
    return this.observeOn(Schedulers.io())
}

fun <T> Flowable<T>.observeOnSingle(): Flowable<T> {
    return this.observeOn(Schedulers.single())
}

fun <T> Flowable<T>.observeOnMain(): Flowable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

fun <T> Flowable<T>.observeOnNewThread(): Flowable<T> {
    return this.observeOn(Schedulers.newThread())
}

fun <T> Flowable<T>.observeOnTrampoline(): Flowable<T> {
    return this.observeOn(Schedulers.trampoline())
}

fun <T> Flowable<T>.observeOnComputation(): Flowable<T> {
    return this.observeOn(Schedulers.computation())
}

fun <T> Single<T>.subscribeOnIo(): Single<T> {
    return this.subscribeOn(Schedulers.io())
}

fun <T> Single<T>.subscribeOnSingle(): Single<T> {
    return this.subscribeOn(Schedulers.single())
}

fun <T> Single<T>.subscribeOnMain(): Single<T> {
    return this.subscribeOn(AndroidSchedulers.mainThread())
}

fun <T> Single<T>.subscribeOnNewThread(): Single<T> {
    return this.subscribeOn(Schedulers.newThread())
}

fun <T> Single<T>.subscribeOnTrampoline(): Single<T> {
    return this.subscribeOn(Schedulers.trampoline())
}

fun <T> Single<T>.subscribeOnComputation(): Single<T> {
    return this.subscribeOn(Schedulers.computation())
}

fun <T> Observable<T>.subscribeOnIo(): Observable<T> {
    return this.subscribeOn(Schedulers.io())
}

fun <T> Observable<T>.subscribeOnSingle(): Observable<T> {
    return this.subscribeOn(Schedulers.single())
}

fun <T> Observable<T>.subscribeOnMain(): Observable<T> {
    return this.subscribeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.subscribeOnNewThread(): Observable<T> {
    return this.subscribeOn(Schedulers.newThread())
}

fun <T> Observable<T>.subscribeOnTrampoline(): Observable<T> {
    return this.subscribeOn(Schedulers.trampoline())
}

fun <T> Observable<T>.subscribeOnComputation(): Observable<T> {
    return this.subscribeOn(Schedulers.computation())
}

fun <T> Flowable<T>.subscribeOnIo(): Flowable<T> {
    return this.subscribeOn(Schedulers.io())
}

fun <T> Flowable<T>.subscribeOnSingle(): Flowable<T> {
    return this.subscribeOn(Schedulers.single())
}

fun <T> Flowable<T>.subscribeOnMain(): Flowable<T> {
    return this.subscribeOn(AndroidSchedulers.mainThread())
}

fun <T> Flowable<T>.subscribeOnNewThread(): Flowable<T> {
    return this.subscribeOn(Schedulers.newThread())
}

fun <T> Flowable<T>.subscribeOnTrampoline(): Flowable<T> {
    return this.subscribeOn(Schedulers.trampoline())
}

fun <T> Flowable<T>.subscribeOnComputation(): Flowable<T> {
    return this.subscribeOn(Schedulers.computation())
}

fun <T> Flowable<T>.toIoAndMain(): Flowable<T> {
    return this.subscribeOnIo().observeOnMain()
}

fun <T> Observable<T>.toIoAndMain(): Observable<T> {
    return this.subscribeOnIo().observeOnMain()
}

fun <T> Single<T>.toIoAndMain(): Single<T> {
    return this.subscribeOnIo().observeOnMain()
}
