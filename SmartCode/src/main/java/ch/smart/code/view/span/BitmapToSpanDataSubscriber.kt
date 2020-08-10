package ch.smart.code.view.span

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import com.blankj.utilcode.util.Utils
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import ch.smart.code.util.pt
import timber.log.Timber

abstract class BitmapToSpanDataSubscriber(
        private val loadTag: String,
        private val iconKey: String,
        private val cacheKey: String,
        private val fixHeight: Float,
        private val endSpace: Float
) : BaseBitmapDataSubscriber() {
    
    override fun onNewResultImpl(bitmap: Bitmap?) {
        var span: SpanData? = null
        if (bitmap != null) {
            try {
                val context = Utils.getApp()
                val oldWidth = bitmap.width
                val oldHeight = bitmap.height
                val ratio = fixHeight.pt.toFloat() / oldHeight.toFloat()
                val icon = BitmapDrawable(context.resources,
                        if (ratio > 0 && ratio != 1.0f) {
                            Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, Matrix().apply { preScale(ratio, ratio) }, true)
                        } else {
                            //这里需要重新创建一份新的bitmap，避免源bitmap因为被加载框架给释放掉后引起的绘制崩溃
                            Bitmap.createBitmap(bitmap)
                        }
                )
                SpanCacheManager.putIcon(cacheKey, icon)
                span = SpanData(icon = icon, endSpace = endSpace.pt)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        if (span != null) {
            onNewResultImpl(loadTag, iconKey, span)
        } else {
            onFailureImpl(loadTag, iconKey)
        }
    }
    
    abstract fun onNewResultImpl(load_tag: String, icon_key: String, span: SpanData)
    
    override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
        onFailureImpl(loadTag, iconKey)
    }
    
    abstract fun onFailureImpl(load_tag: String, icon_key: String)
    
}