package ch.smart.code.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import ch.smart.code.R

class ListLoadingView : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs,
        defStyleAttr
    )

    init {
        inflate(context, R.layout.public_list_loading_view, this)
        gravity = Gravity.CENTER_HORIZONTAL
        orientation = VERTICAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setPadding(paddingLeft, (measuredHeight * 0.3).toInt(), paddingRight, paddingBottom)
    }

}
