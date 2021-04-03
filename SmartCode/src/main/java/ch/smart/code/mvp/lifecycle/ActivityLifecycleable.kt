package ch.smart.code.mvp.lifecycle

import com.trello.rxlifecycle3.android.ActivityEvent

/**
 * 类描述：Activity生命周期管理
 */
interface ActivityLifecycleable : Lifecycleable<ActivityEvent> {
}