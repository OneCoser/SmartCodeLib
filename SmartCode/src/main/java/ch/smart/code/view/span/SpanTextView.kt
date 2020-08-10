package ch.smart.code.view.span

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import ch.smart.code.R
import ch.smart.code.imageloader.downLoadForUiThread
import com.facebook.common.util.UriUtil
import online.daoshang.util.isNotNullOrBlank
import online.daoshang.util.pt
import timber.log.Timber
import java.util.*

open class SpanTextView : AppCompatTextView {

    interface OnSpanTextListener {
        fun onClickSpanItem(txt: String?, data: Any?)
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var loadTag: String = ""
    private var callShow: Boolean = true
    private var txtNeedCenter: Boolean = false
    var spanTextListener: OnSpanTextListener? = null
    private val items = mutableListOf<String>()
    private val spanData = mutableMapOf<String, SpanData>()

    @Synchronized
    open fun reset(): SpanTextView {
        loadTag = UUID.randomUUID().toString()
        callShow = false
        txtNeedCenter = false
        movementMethod = null
        spanData.clear()
        items.clear()
        text = null
        return this
    }

    @JvmOverloads
    fun addIcon(iconRes: Int, fixHeight: Float = 16f, endSpace: Float = 0f): SpanTextView {
        if (iconRes == 0) {
            return this
        }
        return addIcon(UriUtil.getUriForResourceId(iconRes).toString(), fixHeight, endSpace)
    }

    @JvmOverloads
    fun addIcon(iconUrl: String?, fixHeight: Float = 16f, endSpace: Float = 0f): SpanTextView {
        if (iconUrl.isNotNullOrBlank()) {
            if (items.isEmpty()) {
                txtNeedCenter = true
            }
            val cacheKey = String.format("%s-%s", iconUrl, fixHeight)
            val iconKey = String.format("%s-%s", loadTag, cacheKey)
            items.add(iconKey)
            val icon = SpanCacheManager.getIcon(cacheKey)
            if (icon != null) {
                spanData[iconKey] = SpanData(icon = icon, endSpace = endSpace.pt)
                checkShow()
            } else {
                downLoadForUiThread(
                    iconUrl,
                    object : BitmapToSpanDataSubscriber(
                        loadTag,
                        iconKey,
                        cacheKey,
                        fixHeight,
                        endSpace
                    ) {

                        override fun onNewResultImpl(
                            load_tag: String,
                            icon_key: String,
                            span: SpanData
                        ) {
                            if (load_tag == loadTag) {
                                spanData[icon_key] = span
                                checkShow()
                            }
                        }

                        override fun onFailureImpl(load_tag: String, icon_key: String) {
                            Timber.i("onFailureImpl-%s", icon_key)
                        }
                    })
            }
        }
        return this
    }

    @JvmOverloads
    fun addTxt(
        @StringRes txtId: Int,
        @StyleRes styleId: Int = 0,
        clickData: Any? = null,
        clickColor: Int = Color.BLACK,
        endSpace: Int = 0
    ): SpanTextView {
        if (txtId == 0) {
            return this
        }
        return addTxt(
            context.getString(txtId),
            styleId = styleId,
            clickData = clickData,
            clickColor = clickColor,
            endSpace = endSpace
        )
    }

    @JvmOverloads
    fun addTxt(
        txt: String?,
        @StyleRes styleId: Int = 0,
        clickData: Any? = null,
        clickColor: Int = Color.BLACK,
        endSpace: Int = 0
    ): SpanTextView {
        if (txt.isNotNullOrBlank()) {
            if (styleId != 0 || endSpace > 0 || clickData != null) {
                spanData[txt] = SpanData(
                    txt = txt,
                    txtStyleId = styleId,
                    endSpace = if (endSpace > 0) endSpace else 0,
                    clickData = clickData,
                    clickColor = clickColor
                )
                if (clickData != null && movementMethod == null) {
                    movementMethod = LinkMovementMethod.getInstance()
                    highlightColor = Color.TRANSPARENT
                }
            }
            items.add(txt)
            checkShow()
        }
        return this
    }

    @Synchronized
    private fun checkShow() {
        if (callShow) {
            show()
        }
    }

    @Synchronized
    open fun show() {
        callShow = true
        if (items.isNullOrEmpty()) {
            this.text = null
            return
        }
        try {
            val sb = SpanBuilder()
            if (txtNeedCenter) {
                sb.append(".")
                sb.builder.apply {
                    setSpan(
                        TextAppearanceSpan(context, R.style.public_span_text_start),
                        0,
                        length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            items.forEach { item ->
                val span = spanData[item]
                if (span != null) {
                    if (span.txt.isNotNullOrBlank()) {
                        sb.append(span.txt)
                        if (span.clickData != null) {
                            sb.setClickSpan(
                                SpanClickItem(
                                    txt = span.txt,
                                    data = span.clickData,
                                    txtColor = span.clickColor
                                )
                            )
                        }
                        if (span.txtStyleId != 0) {
                            sb.builder.apply {
                                setSpan(
                                    TextAppearanceSpan(context, span.txtStyleId),
                                    length - span.txt.length,
                                    length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                        }
                    }
                    span.icon?.let { icon ->
                        sb.appendImage(icon, SpanBuilder.ALIGN_CENTER)
                    }
                    if (span.endSpace > 0) {
                        sb.appendSpace(span.endSpace)
                    }
                } else if (!item.startsWith(loadTag)) {
                    sb.append(item)
                }
            }
            this.text = sb.builder
        } catch (e: Exception) {
            Timber.e(e)
            this.text = null
        }
    }

    fun isNotEmpty(): Boolean {
        return items.isNotEmpty() || text.isNotNullOrBlank()
    }

    //获取段句间隔
    fun measureSpace(lineSpanCount: Int, lineTextCount: Int, maxWidth: Int): Int {
        if (lineSpanCount <= 1) return 0
        val txtWidth = paint.measureText("宽") //单个文字宽度
        val endWidth = maxWidth - txtWidth * lineTextCount //一行展示完文字后剩余宽度
        return (endWidth / (lineSpanCount - 1)).toInt()
    }

}