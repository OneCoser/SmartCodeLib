package ch.smart.code.util

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Color
import android.net.Uri
import android.webkit.MimeTypeMap
import ch.smart.code.R
import ch.smart.code.util.selector.CompressImageEngine
import ch.smart.code.util.selector.CropImageEngine
import ch.smart.code.util.selector.GlideImageEngine
import ch.smart.code.util.selector.MediaEditInterceptListener
import com.blankj.utilcode.util.Utils
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.style.SelectMainStyle
import com.luck.picture.lib.style.TitleBarStyle
import com.luck.picture.lib.utils.StyleUtils
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 类描述：媒体文件选择
 */

@JvmOverloads
fun openSelector(
    model: PictureSelectionModel,
    listener: OnResultCallbackListener<LocalMedia>,
    maxCount: Int = 9,
    minCount: Int = 1,
    isOriginalControl: Boolean = true,
    style: PictureSelectorStyle? = null,
    cropEngine: CropImageEngine? = null,
    compressEngine: CompressImageEngine? = null,
    editListener: MediaEditInterceptListener? = null,
) {
    model.setLanguage(LanguageConfig.SYSTEM_LANGUAGE)//设置相册语言
        .setSelectorUIStyle(style ?: createWeChatSelectorStyle())//设置相册主题
        .setImageEngine(GlideImageEngine.instance)//设置相册图片加载引擎
        .setCropEngine(cropEngine)
        .setCompressEngine(compressEngine)
        .setEditMediaInterceptListener(editListener)
//        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)//设置屏幕旋转方向
        .setImageSpanCount(4)//相册列表每行显示个数
        .isEmptyResultReturn(false)//支持未选择返回
        .isGif(false)//是否显示gif文件
        .isAutoVideoPlay(false)//预览视频是否自动播放
        .isWithSelectVideoImage(false)//是否支持视频图片同选
        .isPageStrategy(true)//是否开启分页模式
        .isMaxSelectEnabledMask(true)//达到最大选择数是否开启禁选蒙层
        .isCameraForegroundService(true)//拍照时是否开启一个前台服务
        .isPreviewImage(true)//是否支持预览图片
        .isPreviewVideo(true)//是否支持预览视频
        .isDisplayCamera(true)//是否显示相机入口
        .isDirectReturnSingle(true)//单选时是否立即返回
        .isCameraRotateImage(true)//拍照是否纠正旋转图片
        .isFilterSizeDuration(true)//过滤视频小于1秒和文件小于1kb
        .isVideoPauseResumePlay(true)//视频支持暂停与播放
        .isOriginalControl(isOriginalControl)
    if (maxCount > 1) {
        model.setSelectionMode(SelectModeConfig.MULTIPLE)
            .setMaxSelectNum(max(maxCount, minCount))
            .setMinSelectNum(min(maxCount, max(minCount, 1)))
            .setMaxVideoSelectNum(max(maxCount, minCount))
            .setMinVideoSelectNum(min(maxCount, max(minCount, 1)))
    } else {
        model.setSelectionMode(SelectModeConfig.SINGLE)
    }.forResult(listener)
}

@JvmOverloads
fun openImageSelect(
    activity: Activity,
    listener: OnResultCallbackListener<LocalMedia>,
    maxCount: Int = 9,
    minCount: Int = 1,
    isOriginalControl: Boolean = true,
    style: PictureSelectorStyle? = null,
    cropEngine: CropImageEngine? = null,
    compressEngine: CompressImageEngine? = CompressImageEngine(),
    editListener: MediaEditInterceptListener? = MediaEditInterceptListener(style = style),
) {
    openSelector(
        model = PictureSelector.create(activity).openGallery(SelectMimeType.ofImage()),
        listener = listener, maxCount = maxCount, minCount = minCount,
        isOriginalControl = isOriginalControl, style = style,
        cropEngine = cropEngine, compressEngine = compressEngine, editListener = editListener
    )
}

@JvmOverloads
fun openVideoSelect(
    activity: Activity,
    listener: OnResultCallbackListener<LocalMedia>,
    maxCount: Int = 9,
    minCount: Int = 1,
    isOriginalControl: Boolean = false,
    style: PictureSelectorStyle? = null,
) {
    openSelector(
        model = PictureSelector.create(activity).openGallery(SelectMimeType.ofVideo()),
        listener = listener, maxCount = maxCount, minCount = minCount,
        isOriginalControl = isOriginalControl, style = style,
    )
}

@JvmOverloads
fun openCamera(
    activity: Activity,
    listener: OnResultCallbackListener<LocalMedia>,
    isImage: Boolean = true,
) {
    PictureSelector.create(activity)
        .openCamera(if (isImage) SelectMimeType.ofImage() else SelectMimeType.ofVideo())
        .setLanguage(LanguageConfig.SYSTEM_LANGUAGE)
        .isCameraForegroundService(true)//拍照时是否开启一个前台服务
        .isOriginalControl(true)
        .forResult(listener)
}

fun LocalMedia?.checkGetPath(): String? {
    if (this == null) return null
    if (this.isOriginal && this.realPath.isNotNullOrBlank()) {
        return this.realPath
    }
    if (this.isCut && this.cutPath.isNotNullOrBlank()) {
        return this.cutPath
    }
    if (this.isCompressed && this.compressPath.isNotNullOrBlank()) {
        return this.compressPath
    }
    return this.realPath
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
                .getMimeTypeFromExtension(ext.lowercase(Locale.getDefault()))
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

//创建白色主题选择器样式
fun createWhiteSelectorStyle(): PictureSelectorStyle {
    val style = PictureSelectorStyle()
    style.titleBarStyle = TitleBarStyle().apply {
        this.titleBackgroundColor = R.color.ps_color_white.color()
        this.titleDrawableRightResource = R.drawable.public_ic_orange_arrow_down
        this.titleLeftBackResource = R.drawable.ps_ic_black_back
        this.titleTextColor = R.color.ps_color_black.color()
        this.titleCancelTextColor = R.color.ps_color_53575e.color()
        this.isDisplayTitleBarLine = true
    }
    style.bottomBarStyle = BottomNavBarStyle().apply {
        this.bottomNarBarBackgroundColor = Color.parseColor("#EEEEEE")
        this.bottomPreviewSelectTextColor = R.color.ps_color_53575e.color()
        this.bottomPreviewNormalTextColor = R.color.ps_color_9b.color()
        this.bottomPreviewSelectTextColor = R.color.ps_color_fa632d.color()
        this.isCompleteCountTips = false
        this.bottomEditorTextColor = R.color.ps_color_53575e.color()
        this.bottomOriginalTextColor = R.color.ps_color_53575e.color()
    }
    style.selectMainStyle = SelectMainStyle().apply {
        this.statusBarColor = R.color.ps_color_white.color()
        this.isDarkStatusBarBlack = true
        this.selectNormalTextColor = R.color.ps_color_9b.color()
        this.selectTextColor = R.color.ps_color_fa632d.color()
        this.previewSelectBackground = R.drawable.ps_checkbox_selector
        this.selectBackground = R.drawable.ps_checkbox_selector
//        this.selectText = R.string.ps_done_front_num.string()
        this.mainListBackgroundColor = R.color.ps_color_white.color()
    }
    return style
}

//创建微信主题选择器样式
fun createWeChatSelectorStyle(): PictureSelectorStyle {
    val style = PictureSelectorStyle()
    style.titleBarStyle = TitleBarStyle().apply {
        this.isHideCancelButton = true
        this.isAlbumTitleRelativeLeft = true
        this.titleAlbumBackgroundResource = R.drawable.ps_album_bg
        this.titleDrawableRightResource = R.drawable.ps_ic_grey_arrow
        this.previewTitleLeftBackResource = R.drawable.ps_ic_normal_back
    }
    style.bottomBarStyle = BottomNavBarStyle().apply {
        this.bottomPreviewNarBarBackgroundColor = R.color.ps_color_half_grey.color()
//        this.bottomPreviewNormalText = R.string.ps_preview.string()
        this.bottomPreviewNormalTextColor = R.color.ps_color_9b.color()
        this.bottomPreviewNormalTextSize = 16
        this.isCompleteCountTips = false
//        this.bottomPreviewSelectText = R.string.ps_preview_num.string()
        this.bottomPreviewSelectTextColor = R.color.ps_color_white.color()
    }
    style.selectMainStyle = SelectMainStyle().apply {
        this.isSelectNumberStyle = true
        this.isPreviewSelectNumberStyle = false
        this.isPreviewDisplaySelectGallery = true
        this.selectBackground = R.drawable.ps_default_num_selector
        this.previewSelectBackground = R.drawable.ps_preview_checkbox_selector
        this.selectNormalBackgroundResources = R.drawable.ps_select_complete_normal_bg
        this.selectNormalTextColor = R.color.ps_color_53575e.color()
//        this.selectNormalText = R.string.ps_send.string()
        this.adapterPreviewGalleryBackgroundResource = R.drawable.ps_preview_gallery_bg
        this.adapterPreviewGalleryItemSize = 52.dp
//        this.previewSelectText = R.string.ps_select.string()
        this.previewSelectTextSize = 14
        this.previewSelectTextColor = R.color.ps_color_white.color()
        this.previewSelectMarginRight = 6.dp
        this.selectBackgroundResources = R.drawable.ps_select_complete_bg
//        this.selectText = R.string.ps_send_num.string()
        this.selectTextColor = R.color.ps_color_white.color()
        this.mainListBackgroundColor = R.color.ps_color_black.color()
        this.isCompleteSelectRelativeTop = true
        this.isPreviewSelectRelativeBottom = true
        this.isAdapterItemIncludeEdge = false
    }
    return style
}

fun createUCropOptions(style: PictureSelectorStyle? = null): UCrop.Options {
    val options = UCrop.Options()
    options.setHideBottomControls(true)//是否显示裁剪菜单栏
    options.setFreeStyleCropEnabled(true)//裁剪框or图片拖动
    options.setShowCropFrame(true)//是否显示裁剪边框
    options.setShowCropGrid(true)//是否显示裁剪框网格
    options.setCircleDimmedLayer(false)//圆形头像裁剪模式
    options.withAspectRatio(-1f, -1f)//裁剪比列
    FileCache.getImageDir()?.absolutePath?.let {
        options.setCropOutputPathDir(it)
    }
    options.isCropDragSmoothToCenter(false)
    options.setSkipCropMimeType(PictureMimeType.ofGIF(), PictureMimeType.ofWEBP())
    options.isForbidCropGifWebp(true)
    options.isForbidSkipMultipleCrop(false)
    options.setMaxScaleMultiplier(100f)
    if (style != null && style.selectMainStyle.statusBarColor != 0) {
        val mainStyle = style.selectMainStyle
        val isDarkStatusBarBlack = mainStyle.isDarkStatusBarBlack
        val statusBarColor = mainStyle.statusBarColor
        options.isDarkStatusBarBlack(isDarkStatusBarBlack)
        if (StyleUtils.checkStyleValidity(statusBarColor)) {
            options.setStatusBarColor(statusBarColor)
            options.setToolbarColor(statusBarColor)
        } else {
            options.setStatusBarColor(R.color.ps_color_grey.color())
            options.setToolbarColor(R.color.ps_color_grey.color())
        }
        val titleBarStyle = style.titleBarStyle
        if (StyleUtils.checkStyleValidity(titleBarStyle.titleTextColor)) {
            options.setToolbarWidgetColor(titleBarStyle.titleTextColor)
        } else {
            options.setToolbarWidgetColor(R.color.ps_color_white.color())
        }
    } else {
        options.setStatusBarColor(R.color.ps_color_grey.color())
        options.setToolbarColor(R.color.ps_color_grey.color())
        options.setToolbarWidgetColor(R.color.ps_color_white.color())
    }
    return options
}
