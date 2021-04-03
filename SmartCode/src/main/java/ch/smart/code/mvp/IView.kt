package ch.smart.code.mvp

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import ch.smart.code.util.dismissLoading
import ch.smart.code.util.showToast

/**
 * 类描述：View层接口
 */
interface IView {

    @JvmDefault
    fun getCurContext(): Context? {
        return when (this) {
            is Context -> this
            is Fragment -> this.context
            is android.app.Fragment -> this.activity
            is View -> this.context
            else -> null
        }
    }

    @JvmDefault
    fun showLoading() {
        val context = getCurContext()
        if (context != null) {
            ch.smart.code.util.showLoading(context)
        }
    }

    @JvmDefault
    fun hideLoading() {
        dismissLoading()
    }

    @JvmDefault
    fun showMessage(message: String) {
        showToast(message)
    }

    @JvmDefault
    fun showMessage(@StringRes messageResId: Int) {
        showToast(messageResId)
    }

    @JvmDefault
    fun killMyself() {
        if (this is Activity) {
            this.finish()
        } else if (this is Fragment) {
            activity?.finish()
        }
    }
}