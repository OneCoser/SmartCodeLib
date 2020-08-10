package ch.smart.code.dialog

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import ch.smart.code.R
import ch.smart.code.view.span.SpanTextView
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButtonDrawable
import online.daoshang.util.isNotNullOrBlank

open class MsgSimpleAlert(context: Context) : BaseAlert(context) {

    interface MsgSimpleAlertClickListener {
        fun onClick(alert: MsgSimpleAlert)
    }

    private var loadingV: View? = null
    private var loadingHintV: TextView? = null
    private var titleV: SpanTextView? = null
    private var contentV: TextView? = null
    private var simpleBT: QMUIRoundButton? = null

    override fun getLayoutId(): Int {
        return R.layout.public_alert_msg_simple
    }

    override fun initView(rootView: View) {
        titleV = rootView.findViewById(R.id.alert_title)
        contentV = rootView.findViewById(R.id.alert_msg)
        simpleBT = rootView.findViewById(R.id.alert_button)
        loadingV = rootView.findViewById(R.id.alert_loading)
        loadingHintV = rootView.findViewById(R.id.alert_loading_hint)
        loadingV?.setOnClickListener {
        }
        setTitle("")
        hideLoading()
    }

    @JvmOverloads
    fun setTitle(
        txt: String,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        iconUrl: String? = null,
        iconFixHeight: Float = 16f
    ): MsgSimpleAlert {
        titleV?.apply {
            reset()
            addTxt(txt)
            setTextColor(ContextCompat.getColor(context, txtColorId))
            if (iconUrl.isNotNullOrBlank()) {
                if (txt.isNotNullOrBlank()) {
                    addTxt("\n")
                }
                addIcon(iconUrl, fixHeight = iconFixHeight)
            }
            show()
            visibility = if (isNotEmpty()) View.VISIBLE else View.GONE
        }
        return this
    }

    @JvmOverloads
    fun setTitle(
        @StringRes txtId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        iconUrl: String? = null,
        iconFixHeight: Float = 16f
    ): MsgSimpleAlert {
        return setTitle(context.getString(txtId), txtColorId, iconUrl, iconFixHeight)
    }

    @JvmOverloads
    fun setMsg(
        msg: String,
        @ColorRes txtColorId: Int = R.color.public_color_333333
    ): MsgSimpleAlert {
        contentV?.let {
            it.text = msg
            it.setTextColor(ContextCompat.getColor(context, txtColorId))
        }
        return this
    }

    @JvmOverloads
    fun setMsg(
        @StringRes msgId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_333333
    ): MsgSimpleAlert {
        return setMsg(context.getString(msgId), txtColorId)
    }

    @JvmOverloads
    fun setButton(
            txt: String,
            @ColorRes txtColorId: Int = R.color.public_color_FF9500,
            @ColorRes bgColorId: Int = R.color.public_color_FFFFFF,
            listener: MsgSimpleAlertClickListener? = null
    ): MsgSimpleAlert {
        simpleBT?.let {
            it.text = txt
            it.setTextColor(ContextCompat.getColor(context, txtColorId))
            val drawable = it.background as QMUIRoundButtonDrawable
            drawable.setBgData(ColorStateList.valueOf(ContextCompat.getColor(context, bgColorId)))
            it.setOnClickListener { v ->
                if (listener != null) {
                    listener.onClick(this@MsgSimpleAlert)
                } else {
                    dismiss()
                }
            }
        }
        return this
    }

    @JvmOverloads
    fun setButton(
        @StringRes txtId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        @ColorRes bgColorId: Int = R.color.public_color_FFEC3D,
        listener: MsgSimpleAlertClickListener? = null
    ): MsgSimpleAlert {
        return setButton(context.getString(txtId), txtColorId, bgColorId, listener)
    }

    fun showLoading(@StringRes hintRes: Int): MsgSimpleAlert {
        return showLoading(context.getString(hintRes))
    }

    fun showLoading(hint: String? = ""): MsgSimpleAlert {
        loadingV?.visibility = View.VISIBLE
        loadingHintV?.text = hint
        return this
    }

    fun hideLoading(): MsgSimpleAlert {
        loadingV?.visibility = View.GONE
        return this
    }
}
