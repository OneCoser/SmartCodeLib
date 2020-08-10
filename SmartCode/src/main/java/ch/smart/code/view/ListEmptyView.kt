package ch.smart.code.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.smart.code.R
import kotlinx.android.synthetic.main.public_list_empty_view.view.*

class ListEmptyView : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs,
        defStyleAttr
    )

    init {
        inflate(context, R.layout.public_list_empty_view, this)
        gravity = Gravity.CENTER_HORIZONTAL
        orientation = VERTICAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (paddingTop == 0) {
            setPadding(paddingLeft, (measuredHeight * 0.3).toInt(), paddingRight, paddingBottom)
        }
    }

    fun setEmptyImage(@DrawableRes resId: Int) {
        ivEmpty.setImageResource(resId)
    }

    fun setEmptyText(@StringRes resId: Int) {
        tvEmptyText.setText(resId)
    }

    fun setEmptyText(text: String) {
        tvEmptyText.text = text
    }
}
