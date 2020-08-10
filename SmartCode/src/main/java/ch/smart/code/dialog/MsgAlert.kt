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
import ch.smart.code.util.isNotNullOrBlank

open class MsgAlert(context: Context) : BaseAlert(context) {

    interface MsgAlertClickListener {
        fun onClick(alert: MsgAlert)
    }

    private var loadingV: View? = null
    private var loadingHintV: TextView? = null
    private var titleV: SpanTextView? = null
    private var contentV: TextView? = null
    private var contentHintV: TextView? = null
    private var leftBT: QMUIRoundButton? = null
    private var rightBT: QMUIRoundButton? = null

    override fun getLayoutId(): Int {
        return R.layout.public_alert_msg
    }

    override fun initView(rootView: View) {
        titleV = rootView.findViewById(R.id.alert_title)
        contentV = rootView.findViewById(R.id.alert_msg)
        contentHintV = rootView.findViewById(R.id.alert_msg_hint)
        leftBT = rootView.findViewById(R.id.alert_left_button)
        rightBT = rootView.findViewById(R.id.alert_right_button)
        loadingV = rootView.findViewById(R.id.alert_loading)
        loadingHintV = rootView.findViewById(R.id.alert_loading_hint)
        loadingV?.setOnClickListener {
        }
        contentHintV?.visibility = View.GONE
        setTitle("")
        hideLoading()
    }

    @JvmOverloads
    fun setTitle(
        txt: String,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        iconUrl: String? = null,
        iconFixHeight: Float = 16f
    ): MsgAlert {
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
    ): MsgAlert {
        return setTitle(context.getString(txtId), txtColorId, iconUrl, iconFixHeight)
    }

    @JvmOverloads
    fun setMsg(msg: String, @ColorRes txtColorId: Int = R.color.public_color_333333): MsgAlert {
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
    ): MsgAlert {
        return setMsg(context.getString(msgId), txtColorId)
    }

    @JvmOverloads
    fun setMsgHint(
        hint: String,
        @ColorRes txtColorId: Int = R.color.public_color_999999
    ): MsgAlert {
        contentHintV?.let {
            it.text = hint
            it.setTextColor(ContextCompat.getColor(context, txtColorId))
            it.visibility = if (hint.isNotNullOrBlank()) View.VISIBLE else View.GONE
        }
        return this
    }

    @JvmOverloads
    fun setMsgHint(
        @StringRes hintId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_999999
    ): MsgAlert {
        return setMsgHint(context.getString(hintId), txtColorId)
    }

    @JvmOverloads
    fun setLeftButton(
        txt: String,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        @ColorRes bgColorId: Int = R.color.public_color_FFFFFF,
        listener: MsgAlertClickListener? = null
    ): MsgAlert {
        leftBT?.let {
            it.text = txt
            it.setTextColor(ContextCompat.getColor(context, txtColorId))
            val drawable = it.background as QMUIRoundButtonDrawable
            drawable.setBgData(ColorStateList.valueOf(ContextCompat.getColor(context, bgColorId)))
            it.setOnClickListener { v ->
                if (listener != null) {
                    listener.onClick(this@MsgAlert)
                } else {
                    dismiss()
                }
            }
        }
        return this
    }

    @JvmOverloads
    fun setLeftButton(
        @StringRes txtId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        @ColorRes bgColorId: Int = R.color.public_color_FFFFFF,
        listener: MsgAlertClickListener? = null
    ): MsgAlert {
        return setLeftButton(context.getString(txtId), txtColorId, bgColorId, listener)
    }

    @JvmOverloads
    fun setRightButton(
            txt: String,
            @ColorRes txtColorId: Int = R.color.public_color_FF9500,
            @ColorRes bgColorId: Int = R.color.public_color_FFFFFF,
            listener: MsgAlertClickListener? = null
    ): MsgAlert {
        rightBT?.let {
            it.text = txt
            it.setTextColor(ContextCompat.getColor(context, txtColorId))
            val drawable = it.background as QMUIRoundButtonDrawable
            drawable.setBgData(ColorStateList.valueOf(ContextCompat.getColor(context, bgColorId)))
            it.setOnClickListener { v ->
                if (listener != null) {
                    listener.onClick(this@MsgAlert)
                } else {
                    dismiss()
                }
            }
        }
        return this
    }

    @JvmOverloads
    fun setRightButton(
            @StringRes txtId: Int,
            @ColorRes txtColorId: Int = R.color.public_color_FF9500,
            @ColorRes bgColorId: Int = R.color.public_color_FFFFFF,
            listener: MsgAlertClickListener? = null
    ): MsgAlert {
        return setRightButton(context.getString(txtId), txtColorId, bgColorId, listener)
    }

    fun showLoading(@StringRes hintRes: Int): MsgAlert {
        return showLoading(context.getString(hintRes))
    }

    fun showLoading(hint: String? = ""): MsgAlert {
        loadingV?.visibility = View.VISIBLE
        loadingHintV?.text = hint
        return this
    }

    fun hideLoading(): MsgAlert {
        loadingV?.visibility = View.GONE
        return this
    }
}
