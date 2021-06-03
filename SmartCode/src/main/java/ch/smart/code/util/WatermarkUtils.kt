package ch.smart.code.util

import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import ch.smart.code.bean.WatermarkBean
import io.reactivex.Observable
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 类描述：图片增加水印信息
 */
object WatermarkUtils {

    private const val MAX_WIDTH = 1280
    private const val MAX_HEIGHT = 2560

    fun buildObs(image: String, watermark: List<WatermarkBean>?): Observable<String> {
        return buildObs(listOf(image), watermark).map { it.firstOrNull() ?: image }
    }

    fun buildObs(images: List<String>, watermark: List<WatermarkBean>?): Observable<List<String>> {
        return Observable.just(images).map { build(it, watermark) }
    }

    fun build(image: String, watermark: List<WatermarkBean>?): String {
        return build(listOf(image), watermark).firstOrNull() ?: image
    }

    fun build(images: List<String>, watermark: List<WatermarkBean>?): List<String> {
        if (watermark.isNullOrEmpty()) {
            return images
        }
        return images.map { path -> addWatermark(path, watermark) }
    }

    private fun addWatermark(path: String, watermark: List<WatermarkBean>): String {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            options.inSampleSize = options.calculateInSampleSize()
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeFile(path, options)
            val w = bitmap.width
            val h = bitmap.height
            val watermarkBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(watermarkBitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            watermark.forEach {
                drawWatermark(canvas, w, h, it)
            }
            canvas.save()
            val newFile = File(
                FileCache.getImageDir(),
                String.format(
                    "watermark_%s.jpeg",
                    File(path).nameWithoutExtension.replace(" ", "")
                )
            )
            if (newFile.exists()) {
                newFile.delete()
            }
            val stream = ByteArrayOutputStream()
            watermarkBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val fos = FileOutputStream(newFile.absolutePath)
            fos.write(stream.toByteArray())
            fos.flush()
            fos.close()
            if (newFile.exists()) {
                return newFile.absolutePath
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return path
    }

    private fun drawWatermark(canvas: Canvas, w: Int, h: Int, watermark: WatermarkBean) {
        if (watermark.text.isNullOrBlank()) {
            return
        }
        try {
            //文字样式
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.DEV_KERN_TEXT_FLAG)
            textPaint.color = watermark.color.color()//设置文字颜色
            textPaint.typeface = Typeface.DEFAULT//设置字体样式
            textPaint.textSize = watermark.size.size(h)//设置字体大小
            val bgPaint = if (watermark.bgColor?.startsWith("#") == true) {
                Paint().apply {
                    this.isAntiAlias = true
                    this.style = Paint.Style.FILL
                    this.color = watermark.bgColor.color(defaultColor = Color.TRANSPARENT)
                }
            } else {
                null
            }
            val marginLeft = watermark.marginLeft.size(w)
            val marginRight = watermark.marginRight.size(w)
            val marginTop = watermark.marginTop.size(h)
            val marginBottom = watermark.marginBottom.size(h)
            //是否需要自动换行
            canvas.save()
            if (watermark.autoLine) {
                val layoutWidth = (w - marginLeft - marginRight).toInt()//文字最大绘制宽度
                val layout = watermark.text.layout(textPaint, layoutWidth)
                //移动画布
                canvas.translate(
                    marginLeft, when (watermark.gravity) {
                        WatermarkBean.GRAVITY_CENTER -> (h - layout.height) / 2f
                        WatermarkBean.GRAVITY_BOTTOM -> h - marginBottom - layout.height
                        else -> marginTop
                    }
                )
                //绘制背景色
                bgPaint?.let { bg ->
                    canvas.drawRect(Rect(0, 0, layoutWidth, layout.height), bg)
                }
                //绘制文字
                layout.draw(canvas)
            } else {
                val fontMetrics = textPaint.fontMetrics
                val space = abs(fontMetrics.bottom)
                val textHeight = space + abs(fontMetrics.top)
                val textY = when (watermark.gravity) {
                    WatermarkBean.GRAVITY_CENTER -> (h + textHeight - space) / 2f
                    WatermarkBean.GRAVITY_BOTTOM -> h - marginBottom - space
                    else -> marginTop + textHeight - space
                }
                //绘制背景色
                bgPaint?.let { bg ->
                    val textWidth = textPaint.measureText(watermark.text)
                    canvas.drawRect(
                        Rect(
                            marginLeft.toInt(),
                            (textY - textHeight + space).toInt(),
                            min((marginLeft + textWidth).toInt(), w),
                            (textY + space).toInt()
                        ), bg
                    )
                }
                //绘制文字
                canvas.drawText(watermark.text, marginLeft, textY, textPaint)
            }
            canvas.restore()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun BitmapFactory.Options.calculateInSampleSize(): Int {
        // 源图片的高度和宽度
        val height = this.outHeight
        val width = this.outWidth
        if (height > MAX_HEIGHT || width > MAX_WIDTH) {
            // 计算出实际宽高和目标宽高的比率
            val heightRatio = (height.toFloat() / MAX_HEIGHT.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / MAX_WIDTH.toFloat()).roundToInt()
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            return if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return 1
    }

    private fun String?.color(defaultColor: Int = Color.BLACK): Int {
        if (this.isNullOrBlank() || !this.startsWith("#")) return defaultColor
        return try {
            Color.parseColor(this)
        } catch (e: Exception) {
            defaultColor
        }
    }

    private fun Float.size(n: Int): Float {
        return when {
            this >= 1f -> this
            this > 0f -> this * n
            else -> WatermarkBean.DEFAULT_SIZE * n
        }
    }

    private fun String.layout(paint: TextPaint, width: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(this, 0, this.length, paint, width).build()
        } else {
            StaticLayout(
                this, paint, width, Layout.Alignment.ALIGN_NORMAL,
                1f, 0f, true
            )
        }
    }
}