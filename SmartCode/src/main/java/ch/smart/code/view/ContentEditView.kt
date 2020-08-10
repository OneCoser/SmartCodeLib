package ch.smart.code.view

import android.content.Context
import android.graphics.Color
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import ch.smart.code.R
import ch.smart.code.util.showToast
import online.daoshang.util.isNotNullOrBlank
import online.daoshang.util.pt

class ContentEditView : LinearLayout {

    object ContentType {
        const val NONE = 0
        const val EDIT_TXT = 1
        const val EDIT_INT = 2
        const val EDIT_DOUBLE = 3
    }

    object CEVGravity {
        const val START = 0
        const val START_CENTER = 1
        const val START_BOTTOM = 2
        const val CENTER = 3
        const val CENTER_TOP = 4
        const val CENTER_BOTTOM = 5
        const val END = 6
        const val END_CENTER = 7
        const val END_BOTTOM = 8
    }

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init(attrs, 0)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    var titleView: TextView? = null
        private set

    var contentView: TextView? = null
        private set

    private var contentIsFix = false
    private fun init(attrs: AttributeSet?, def: Int) {
        removeAllViews()
        this.orientation = HORIZONTAL
        val a = context.obtainStyledAttributes(attrs, R.styleable.ContentEditView, def, 0)
        //初始化Title
        TextView(context).let {
            titleView = it
            setCEVGravity(
                it,
                a.getInt(
                    R.styleable.ContentEditView_cev_titleGravity,
                    CEVGravity.START
                )
            )
            it.setPadding(0, 0, 0, 0)
            it.setTextColor(
                a.getColor(
                    R.styleable.ContentEditView_cev_titleColor,
                    Color.BLACK
                )
            )
            it.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                a.getInt(R.styleable.ContentEditView_cev_titleSize, 14).toFloat()
            )
            a.getDrawable(R.styleable.ContentEditView_cev_titleIcon)?.let { icon ->
                it.compoundDrawablePadding =
                    a.getInt(R.styleable.ContentEditView_cev_titleIconSpace, 2).pt
                it.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
            }
            it.text = a.getString(R.styleable.ContentEditView_cev_title)
            it.setBackgroundColor(Color.TRANSPARENT)
            val titleWidth = a.getInt(R.styleable.ContentEditView_cev_titleWidth, 0).pt
            addView(
                it, LayoutParams(
                    if (titleWidth > 0) {
                        titleWidth
                    } else {
                        LayoutParams.WRAP_CONTENT
                    }, LayoutParams.WRAP_CONTENT
                )
            )
        }

        //初始化Content
        val contentType = a.getInt(
            R.styleable.ContentEditView_cev_contentType,
            ContentType.NONE
        )
        if (contentType == ContentType.NONE) {
            TextView(context)
        } else {
            EditText(context)
        }.let {
            contentView = it
            setCEVGravity(
                it, a.getInt(
                    R.styleable.ContentEditView_cev_contentGravity,
                    CEVGravity.START_CENTER
                )
            )
            it.setPadding(a.getInt(R.styleable.ContentEditView_cev_space, 8).pt, 0, 0, 0)
            it.setTextColor(
                a.getColor(
                    R.styleable.ContentEditView_cev_contentColor,
                    Color.BLACK
                )
            )
            it.setHintTextColor(
                a.getColor(
                    R.styleable.ContentEditView_cev_contentHintColor,
                    Color.CYAN
                )
            )
            it.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                a.getInt(R.styleable.ContentEditView_cev_contentSize, 14).toFloat()
            )
            it.text = a.getString(R.styleable.ContentEditView_cev_content)
            it.hint = a.getString(R.styleable.ContentEditView_cev_contentHint)
            a.getDrawable(R.styleable.ContentEditView_cev_contentIcon)?.let { icon ->
                it.compoundDrawablePadding =
                    a.getInt(R.styleable.ContentEditView_cev_contentIconSpace, 10).pt
                it.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
            }
            val inputType = when (contentType) {
                ContentType.EDIT_INT -> InputType.TYPE_CLASS_NUMBER
                ContentType.EDIT_DOUBLE -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                else -> InputType.TYPE_CLASS_TEXT
            }
            val maxLine = a.getInt(R.styleable.ContentEditView_cev_contentMaxLine, 1)
            it.inputType = if (maxLine > 1) {
                inputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            } else {
                inputType
            }
            it.maxLines = maxLine
            val maxLength = a.getInt(R.styleable.ContentEditView_cev_contentMaxLength, 0)
            if (maxLength > 0) {
                it.filters = arrayOf<InputFilter>(LengthFilter(maxLength))
            }
            it.setBackgroundColor(Color.TRANSPARENT)
            addView(
                it, LayoutParams(
                    if (a.getBoolean(R.styleable.ContentEditView_cev_contentAutoLayout, false)) {
                        LayoutParams.WRAP_CONTENT
                    } else {
                        LayoutParams.MATCH_PARENT
                    },
                    LayoutParams.WRAP_CONTENT
                )
            )
        }
        a.recycle()
    }

    private fun setCEVGravity(view: TextView, gravity: Int) {
        view.gravity = when (gravity) {
            CEVGravity.START -> Gravity.START or Gravity.TOP
            CEVGravity.START_CENTER -> Gravity.START or Gravity.CENTER_VERTICAL
            CEVGravity.START_BOTTOM -> Gravity.START or Gravity.BOTTOM
            CEVGravity.CENTER -> Gravity.CENTER
            CEVGravity.CENTER_TOP -> Gravity.START or Gravity.CENTER_HORIZONTAL
            CEVGravity.CENTER_BOTTOM -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            CEVGravity.END -> Gravity.END or Gravity.TOP
            CEVGravity.END_CENTER -> Gravity.END or Gravity.CENTER_VERTICAL
            CEVGravity.END_BOTTOM -> Gravity.END or Gravity.BOTTOM
            else -> Gravity.START
        }
    }

    fun setTitle(str: String?) {
        titleView?.text = str
    }

    fun setTitle(@StringRes resId: Int) {
        titleView?.setText(resId)
    }

    fun setContent(@StringRes resId: Int, fixContent: Boolean = false) {
        setContent(context.getString(resId), fixContent = fixContent)
    }

    fun setContent(str: String?, fixContent: Boolean = false) {
        contentView?.text = str
        contentIsFix = fixContent && str.isNotNullOrBlank()
        if (contentView is EditText) {
            contentView?.isEnabled = !contentIsFix
        }
    }

    fun getContent(): String? {
        return contentView?.text?.toString()
    }

    fun contentIsFix(): Boolean {
        return contentIsFix
    }

    fun isNotEmpty(): Boolean {
        return contentView?.text.isNotNullOrBlank()
    }

    fun toastHintForTitle() {
        showToast(
            String.format(
                "%s%s",
                if (contentView is EditText) "请输入" else "请选择",
                titleView?.text
            )
        )
    }

    fun toastHint() {
        val hint = contentView?.hint?.toString() ?: return
        showToast(hint)
    }

    val isVis: Boolean get() = visibility == View.VISIBLE
}