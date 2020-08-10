package ch.smart.code.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import ch.smart.code.R
import timber.log.Timber

/**
 * 类描述：自定义Alert基类
 * 创建人：chenhao
 */
abstract class BaseAlert : Dialog, BaseAlertInterface {
    constructor(context: Context) : super(context, R.style.public_alert) {
        init()
    }

    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        init()
    }

    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener) {
        init()
    }

    fun init() {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window?.setGravity(getGravity())
        val rootView = View.inflate(context, getLayoutId(), null) ?: return
        initView(rootView)
        val w = (getWidth() * getWidthRatio()).toInt()
        setContentView(
            rootView,
            ViewGroup.LayoutParams(
                if (w > 0) w else ViewGroup.LayoutParams.WRAP_CONTENT,
                getHeight()
            )
        )
    }

    fun setCanceledOnTouchOutsideS(b: Boolean): BaseAlert {
        setCanceledOnTouchOutside(b)
        return this
    }

    fun setCancelableS(flag: Boolean): BaseAlert {
        setCancelable(flag)
        return this
    }

    fun setOnCancelListenerS(listener: DialogInterface.OnCancelListener?): BaseAlert {
        setOnCancelListener(listener)
        return this
    }

    fun setOnDismissListenerS(listener: DialogInterface.OnDismissListener?): BaseAlert {
        setOnDismissListener(listener)
        return this
    }

    fun setOnShowListenerS(listener: OnShowListener?): BaseAlert {
        setOnShowListener(listener)
        return this
    }

    override fun show() {
        try {
            if (context is Activity) {
                val activity = context as Activity
                if (!activity.isFinishing) {
                    super.show()
                }
            } else {
                super.show()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}