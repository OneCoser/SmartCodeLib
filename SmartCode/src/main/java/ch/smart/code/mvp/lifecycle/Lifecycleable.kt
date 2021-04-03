package ch.smart.code.mvp.lifecycle

import io.reactivex.subjects.Subject

/**
 * 类描述：生命周期管理
 */
interface Lifecycleable<E> {
    fun provideLifecycleSubject(): Subject<E>
}