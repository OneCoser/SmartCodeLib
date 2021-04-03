package ch.smart.code.util

import android.app.Activity
import android.content.pm.ActivityInfo
import ch.smart.code.R
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener

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
    val safeMaxCount = if (maxCount < minCount) minCount else maxCount
    val selector = PictureSelector.create(activity).openGallery(PictureMimeType.ofImage())
        .imageEngine(GlideImageEngine.getInstance())
    if (safeMaxCount > 1) {
        selector.selectionMode(PictureConfig.MULTIPLE)
            .maxSelectNum(safeMaxCount).minSelectNum(minCount)
    } else {
        selector.selectionMode(PictureConfig.SINGLE)
    }.theme(R.style.picture_default_style).isWeChatStyle(true)
        .isUseCustomCamera(false)//不使用自定义相机，否则需要导入androidxCamera
        .isPageStrategy(true).isMaxSelectEnabledMask(true).imageSpanCount(4)
        .isReturnEmpty(false)
        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .isSingleDirectReturn(true)
        .isPreviewImage(true)
        .isCamera(true)
        .isEnableCrop(needCrop)
        .isCompress(true)
        .isOriginalImageControl(true)
        .isGif(false)
        .forResult(listener)
}

fun LocalMedia?.checkGetPath(): String? {
    if (this?.compressPath.isNotNullOrBlank()) {
        return this?.compressPath
    }
    if (this?.androidQToPath.isNotNullOrBlank()) {
        return this?.androidQToPath
    }
    if (this?.realPath.isNotNullOrBlank()) {
        return this?.realPath
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
    val safeMaxCount = if (maxCount < minCount) minCount else maxCount
    val selector = PictureSelector.create(activity).openGallery(PictureMimeType.ofVideo())
        .imageEngine(GlideImageEngine.getInstance())
    if (safeMaxCount > 1) {
        selector.selectionMode(PictureConfig.MULTIPLE)
            .maxVideoSelectNum(safeMaxCount).minVideoSelectNum(minCount)
    } else {
        selector.selectionMode(PictureConfig.SINGLE)
    }.theme(R.style.picture_default_style).isWeChatStyle(true)
        .isUseCustomCamera(false)//不使用自定义相机，否则需要导入androidxCamera
        .isPageStrategy(true).isMaxSelectEnabledMask(true).imageSpanCount(4)
        .isReturnEmpty(false)
        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .isSingleDirectReturn(true)
        .isPreviewVideo(true).isCamera(true)
        .forResult(listener)
}

@JvmOverloads
fun openCamera(
    activity: Activity,
    listener: OnResultCallbackListener<LocalMedia>,
    isImage: Boolean = true,
    needCrop: Boolean = false
) {
    PictureSelector.create(activity)
        .openCamera(
            if (isImage) {
                PictureMimeType.ofImage()
            } else {
                PictureMimeType.ofVideo()
            }
        )
        .imageEngine(GlideImageEngine.getInstance())
        .theme(R.style.picture_default_style).isWeChatStyle(true)
        .isUseCustomCamera(false)//不使用自定义相机，否则需要导入androidxCamera
        .isReturnEmpty(false).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .isPreviewImage(true).isEnableCrop(needCrop)
        .isPreviewVideo(true)
        .isCompress(true)
        .forResult(listener)
}
