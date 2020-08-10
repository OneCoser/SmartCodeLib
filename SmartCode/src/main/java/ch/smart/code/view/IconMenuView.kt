package ch.smart.code.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.StringRes
import ch.smart.code.R
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import ch.smart.code.util.pt

class IconMenuView : RelativeLayout {

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }
    
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }
    
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }
    
    private var iconV: ImageView? = null
    private var txtV: TextView? = null
    private var unreadV: QMUIRoundButton? = null
    private var animListener: OnClickListener? = null
    
    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        this.removeAllViews()
        val ta = context.obtainStyledAttributes(attrs, R.styleable.IconMenuView, defStyleAttr, 0)
        // 初始化图标View
        iconV = ImageView(context)
        iconV?.id = R.id.public_imv_icon
        val iconW = ta.getDimensionPixelSize(R.styleable.IconMenuView_imv_iconWidth, 0)
        val iconH = ta.getDimensionPixelSize(R.styleable.IconMenuView_imv_iconHeight, 0)
        val iconP = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        iconP.addRule(CENTER_HORIZONTAL)
        if (iconW > 0 && iconH > 0) {
            iconV?.scaleType = ImageView.ScaleType.FIT_CENTER
            iconP.width = iconW
            iconP.height = iconH
        } else {
            iconV?.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        this.addView(iconV, iconP)
        // 初始化文字View
        txtV = TextView(context)
        txtV?.id = R.id.public_imv_txt
        txtV?.setTextSize(TypedValue.COMPLEX_UNIT_SP, ta.getInt(R.styleable.IconMenuView_imv_txtSize, 10).toFloat())
        txtV?.setTextColor(ta.getColor(R.styleable.IconMenuView_imv_txtColor, Color.BLACK))
        val shadowType = ta.getInt(R.styleable.IconMenuView_imv_txtShadowType, 0)
        if (shadowType == 1) {
            txtV?.setShadowLayer(3.0f, 0f, 1f, Color.parseColor("#66000000"))
        }
        val txtP = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        txtP.addRule(CENTER_HORIZONTAL)
        txtP.addRule(BELOW, R.id.public_imv_icon)
        txtP.topMargin = ta.getDimensionPixelSize(R.styleable.IconMenuView_imv_space, 0)
        this.addView(txtV, txtP)
        // 初始化小红点View
        unreadV = View.inflate(context, R.layout.public_imv_unread, null) as? QMUIRoundButton
        unreadV?.id = R.id.public_imv_unread
        val unreadP = LayoutParams(LayoutParams.WRAP_CONTENT, 10.pt)
        unreadP.addRule(ALIGN_TOP, R.id.public_imv_icon)
        unreadP.addRule(ALIGN_END, R.id.public_imv_icon)
        unreadP.marginEnd = -(8.pt)
        this.addView(unreadV, unreadP)
        // 完成设置
        setMenu(ta.getResourceId(R.styleable.IconMenuView_imv_icon, R.drawable.public_loading_anim_ic),
                ta.getText(R.styleable.IconMenuView_imv_txt).toString(),
                ta.getInt(R.styleable.IconMenuView_imv_unreadCount, 0))
        ta.recycle()
    }
    
    fun setMenu(iconRes: Int, @StringRes txtRes: Int, unread: Int = 0) {
        setIcon(iconRes)
        setTxt(txtRes)
        setUnreadCount(unread)
    }
    
    fun setMenu(iconRes: Int, txt: String, unread: Int = 0) {
        setIcon(iconRes)
        setTxt(txt)
        setUnreadCount(unread)
    }
    
    fun setIcon(iconRes: Int) {
        iconV?.setImageResource(iconRes)
    }
    
    fun setTxt(@StringRes txtRes: Int) {
        setTxt(context.getString(txtRes))
    }
    
    fun setTxt(txt: String) {
        txtV?.text = txt
        txtV?.visibility = if (txt.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
    
    fun setUnreadCount(unread: Int) {
        unreadV?.text = if (unread > 99) {
            "99+"
        } else {
            unread.toString()
        }
        unreadV?.visibility = if (unread > 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    
    fun setOnAnimClickListener(listener: OnClickListener) {
        this.animListener = listener
        this.setOnClickListener {
            clickAnim()
        }
    }
    
    private fun clickAnim() {
        if (isClickable) {
            isClickable = false
            iconV?.animate()?.cancel()
            iconV?.clearAnimation()
            iconV?.animate()?.scaleX(0.65f)?.scaleY(0.65f)?.setDuration(150)?.setInterpolator(AnticipateInterpolator())
                    ?.setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            iconV?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(150)?.setInterpolator(OvershootInterpolator())
                                    ?.setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator?) {
                                            super.onAnimationEnd(animation)
                                            iconV?.clearAnimation()
                                            animListener?.onClick(this@IconMenuView)
                                            this@IconMenuView.isClickable = true
                                        }
                                    })?.start()
                        }
                    })?.start()
        }
    }
    
    override fun onDetachedFromWindow() {
        animListener = null
        super.onDetachedFromWindow()
    }
}
