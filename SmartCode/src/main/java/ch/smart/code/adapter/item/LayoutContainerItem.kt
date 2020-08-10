package ch.smart.code.adapter.item

import android.view.View
import kotlinx.android.extensions.LayoutContainer

abstract class LayoutContainerItem<T> : SimpleItem<T>(), LayoutContainer {
    override val containerView: View?
        get() = rootView
    
}
