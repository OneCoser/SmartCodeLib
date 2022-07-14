package ch.smart.code.util.selector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.yalantis.ucrop.UCropImageEngine

class GlideUCropImageEngine : UCropImageEngine {

    companion object {
        val instance by lazy {
            GlideUCropImageEngine()
        }
    }

    override fun loadImage(context: Context?, url: String?, imageView: ImageView?) {
        Glide.with(context ?: return).load(url).into(imageView ?: return)
    }

    override fun loadImage(
        context: Context?,
        url: Uri?,
        maxWidth: Int,
        maxHeight: Int,
        call: UCropImageEngine.OnCallbackListener<Bitmap?>?
    ) {
        Glide.with(context ?: return)
            .asBitmap()
            .load(url)
            .override(maxWidth, maxHeight)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    call?.onCall(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    call?.onCall(null)
                }
            })
    }
}