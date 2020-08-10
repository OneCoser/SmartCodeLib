package ch.smart.code.view.span

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

class SpanClickItem(
    private val txt: String? = null,
    private val data: Any? = null,
    private val txtColor: Int = Color.BLACK
) : ClickableSpan() {

    override fun onClick(widget: View) {
        (widget as? SpanTextView)?.spanTextListener?.onClickSpanItem(txt, data)
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = txtColor
        ds.isUnderlineText = false
    }
}