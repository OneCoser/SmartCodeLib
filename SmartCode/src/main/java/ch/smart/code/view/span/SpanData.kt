package ch.smart.code.view.span

import android.graphics.drawable.Drawable

data class SpanData(
    val txt: String? = null,
    val icon: Drawable? = null,
    val clickData: Any? = null,
    val clickColor: Int = 0,
    val txtStyleId: Int = 0,
    val endSpace: Int = 0
)