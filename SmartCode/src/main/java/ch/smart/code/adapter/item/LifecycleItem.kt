package ch.smart.code.adapter.item

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

abstract class LifecycleItem<T> : LayoutContainerItem<T>(), LifecycleObserver {
    
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun onResume() {
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun onPause() {
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onStop() {
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }
    
}
