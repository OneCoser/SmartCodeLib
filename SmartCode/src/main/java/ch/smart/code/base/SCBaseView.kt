package ch.smart.code.base

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import ch.smart.code.util.dismissLoading
import ch.smart.code.util.showToast
import com.jess.arms.mvp.IView

interface SCBaseView : IView {

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
    override fun showLoading() {
        val context = getCurContext()
        if (context != null) {
            ch.smart.code.util.showLoading(context)
        }
    }

    @JvmDefault
    override fun hideLoading() {
        dismissLoading()
    }

    @JvmDefault
    override fun showMessage(message: String) {
        showToast(message)
    }

    @JvmDefault
    fun showMessage(@StringRes messageResId: Int) {
        showToast(messageResId)
    }

    @JvmDefault
    override fun killMyself() {
        if (this is Activity) {
            this.finish()
        } else if (this is Fragment) {
            activity?.finish()
        }
    }
}
