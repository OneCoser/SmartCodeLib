package ch.smart.code.dialog

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.ScreenUtils

interface BaseAlertInterface {

    fun getLayoutId(): Int

    fun initView(rooView: View)

    /**
     * 弹框显示位置
     */
    fun getGravity(): Int {
        return Gravity.CENTER
    }

    /**
     * 弹框宽度计算比例
     */
    fun getWidthRatio(): Double {
        return 0.8
    }

    /**
     * 弹框宽度，默认为屏幕宽度
     */
    fun getWidth(): Int {
        return ScreenUtils.getScreenWidth()
    }

    /**
     * 弹框高度，默认为自适应
     */
    fun getHeight(): Int {
        return ViewGroup.LayoutParams.WRAP_CONTENT
    }
}