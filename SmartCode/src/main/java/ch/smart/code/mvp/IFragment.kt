package ch.smart.code.mvp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * 类描述：
 */
interface IFragment {

    fun useEventBus(): Boolean

    fun initView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?

    fun initData(savedInstanceState: Bundle?)
}