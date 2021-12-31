package ch.smart.code.util

import android.app.Activity
import android.content.ContentResolver
import android.content.pm.ActivityInfo
import android.net.Uri
import android.webkit.MimeTypeMap
import ch.smart.code.R
import com.blankj.utilcode.util.Utils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 类描述：媒体文件选择
 */

@JvmOverloads
fun openImageSelect(
    activity: Activity,
    listener: OnResultCallbackListener<LocalMedia>,
    maxCount: Int = 9,
    minCount: Int = 1,
    needCrop: Boolean = false
) {
    val selector = PictureSelector.create(activity)
        .openGallery(PictureMimeType.ofImage())
        .imageEngine(GlideImageEngine.getInstance())
        .theme(R.style.picture_default_style)
        .isWeChatStyle(true)
        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .isUseCustomCamera(false)//不使用自定义相机，否则需要导入androidxCamera
        .isPageStrategy(true)
        .isMaxSelectEnabledMask(true)
        .imageSpanCount(4)
        .isReturnEmpty(false)
        .isSingleDirectReturn(true)
        .isPreviewImage(true)
        .isCamera(true)
        .isEnableCrop(needCrop)
        .isCompress(true)
        .isOriginalImageControl(true)
        .isGif(false)
    if (maxCount > 1) {
        selector.selectionMode(PictureConfig.MULTIPLE)
            .maxSelectNum(max(maxCount, minCount))
            .minSelectNum(min(maxCount, max(minCount, 1)))
    } else {
        selector.selectionMode(PictureConfig.SINGLE)
    }.forResult(listener)
}

fun LocalMedia?.checkGetPath(): String? {
    if (this == null) return null
    if (this.isOriginal && this.originalPath.isNotNullOrBlank()) {
        return this.originalPath
    }
    if (this.isCut && this.cutPath.isNotNullOrBlank()) {
        return this.cutPath
    }
    if (this.isCompressed && this.compressPath.isNotNullOrBlank()) {
        return this.compressPath
    }
    if (this.realPath.isNotNullOrBlank()) {
        return this.realPath
    }
    return null
}

fun List<LocalMedia>?.getPaths(): List<String> {
    val paths = arrayListOf<String>()
    this?.forEach {
        val path = it.checkGetPath()
        if (path.isNotNullOrBlank()) {
            paths.add(path)
        }
    }
    return paths
}

@JvmOverloads
fun openVideoSelect(
    activity: Activity,
    listener: OnResultCallbackListener<LocalMedia>,
    maxCount: Int = 9,
    minCount: Int = 1
) {
    val selector = PictureSelector.create(activity)
        .openGallery(PictureMimeType.ofVideo())
        .imageEngine(GlideImageEngine.getInstance())
        .theme(R.style.picture_default_style)
        .isWeChatStyle(true)
        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .isUseCustomCamera(false)//不使用自定义相机，否则需要导入androidxCamera
        .isPageStrategy(true)
        .isMaxSelectEnabledMask(true)
        .imageSpanCount(4)
        .isReturnEmpty(false)
        .isSingleDirectReturn(true)
        .isPreviewVideo(true)
        .isCamera(true)
    if (maxCount > 1) {
        selector.selectionMode(PictureConfig.MULTIPLE)
            .maxSelectNum(max(maxCount, minCount))
            .minSelectNum(min(maxCount, max(minCount, 1)))
            .maxVideoSelectNum(max(maxCount, minCount))
            .maxVideoSelectNum(min(maxCount, max(minCount, 1)))
    } else {
        selector.selectionMode(PictureConfig.SINGLE)
    }.forResult(listener)
}

@JvmOverloads
fun openCamera(
    activity: Activity,
    listener: OnResultCallbackListener<LocalMedia>,
    isImage: Boolean = true,
    needCrop: Boolean = false
) {
    PictureSelector.create(activity)
        .openCamera(if (isImage) PictureMimeType.ofImage() else PictureMimeType.ofVideo())
        .imageEngine(GlideImageEngine.getInstance())
        .theme(R.style.picture_default_style)
        .isWeChatStyle(true)
        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .isUseCustomCamera(false)//不使用自定义相机，否则需要导入androidxCamera
        .isReturnEmpty(false)
        .isPreviewImage(true)
        .isEnableCrop(needCrop)
        .isPreviewVideo(true)
        .isCompress(true)
        .forResult(listener)
}

//获取文件媒体类型
fun String?.getMimeType(defaultType: String = "image/jpeg"): String {
    return try {
        val file = File(this ?: return defaultType)
        val uri = Uri.fromFile(file)
        val type = if (uri.scheme?.equals(ContentResolver.SCHEME_CONTENT) == true) {
            Utils.getApp().contentResolver.getType(uri)
        } else {
            val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(ext.toLowerCase(Locale.getDefault()))
        }
        if (type.isNotNullOrBlank()) {
            type
        } else {
            defaultType
        }
    } catch (e: Exception) {
        Timber.e(e)
        defaultType
    }
}
