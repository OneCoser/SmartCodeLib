package ch.smart.code.mvp

import android.os.Bundle

/**
 * 类描述：
 */
interface IActivity {

    fun useEventBus(): Boolean

    fun useFragment(): Boolean

    fun initView(savedInstanceState: Bundle?): Int

    fun initData(savedInstanceState: Bundle?)
}