package ch.smart.code.util.selector

import android.content.Context
import android.widget.ImageView
import ch.smart.code.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.luck.picture.lib.engine.ImageEngine

class GlideImageEngine : ImageEngine {

    companion object {
        val instance by lazy {
            GlideImageEngine()
        }
    }

    override fun loadImage(context: Context?, url: String?, imageView: ImageView?) {
        Glide.with(context ?: return)
            .load(url ?: return)
            .into(imageView ?: return)
    }

    override fun loadImage(
        context: Context?,
        imageView: ImageView?,
        url: String?,
        maxWidth: Int,
        maxHeight: Int
    ) {
        Glide.with(context ?: return)
            .load(url ?: return)
            .override(maxWidth, maxHeight)
            .into(imageView ?: return)
    }

    override fun loadAlbumCover(context: Context?, url: String?, imageView: ImageView?) {
        Glide.with(context ?: return)
            .asBitmap()
            .load(url ?: return)
            .override(180, 180)
            .sizeMultiplier(0.5f)
            .transform(CenterCrop(), RoundedCorners(8))
            .placeholder(R.drawable.ps_image_placeholder)
            .into(imageView ?: return)
    }

    override fun loadGridImage(context: Context?, url: String?, imageView: ImageView?) {
        Glide.with(context ?: return)
            .load(url ?: return)
            .override(200, 200)
            .centerCrop()
            .placeholder(R.drawable.ps_image_placeholder)
            .into(imageView ?: return)
    }

    override fun pauseRequests(context: Context?) {
        Glide.with(context ?: return).pauseRequests()
    }

    override fun resumeRequests(context: Context?) {
        Glide.with(context ?: return).resumeRequests()
    }

}