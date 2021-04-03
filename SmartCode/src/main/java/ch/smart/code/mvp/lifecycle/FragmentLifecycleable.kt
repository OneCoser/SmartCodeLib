package ch.smart.code.mvp.lifecycle

import com.trello.rxlifecycle3.android.FragmentEvent

/**
 * 类描述：Fragment生命周期管理
 */
interface FragmentLifecycleable : Lifecycleable<FragmentEvent> {
}