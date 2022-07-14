package ch.smart.code.util.selector

import android.net.Uri
import androidx.fragment.app.Fragment
import ch.smart.code.util.FileCache
import ch.smart.code.util.createUCropOptions
import ch.smart.code.util.showErrorToast
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnMediaEditInterceptListener
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.utils.DateUtils
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.io.File

/**
 * 项目名称：SmartCodeLib
 * 类描述：自定义编辑监听
 */
open class MediaEditInterceptListener(
    private val style: PictureSelectorStyle? = null
) : OnMediaEditInterceptListener {

    override fun onStartMediaEdit(
        fragment: Fragment?,
        currentLocalMedia: LocalMedia?,
        requestCode: Int
    ) {
        try {
            val currentEditPath = currentLocalMedia?.availablePath ?: return
            val uCrop = UCrop.of<Any>(
                if (PictureMimeType.isContent(currentEditPath)) {
                    Uri.parse(currentEditPath)
                } else {
                    Uri.fromFile(File(currentEditPath))
                }, Uri.fromFile(
                    File(FileCache.getImageDir(), DateUtils.getCreateFileName("CROP_") + ".jpeg")
                )
            )
            val options = createUCropOptions(style = style)
            options.setHideBottomControls(false)
            uCrop.withOptions(options)
            uCrop.setImageEngine(GlideUCropImageEngine.instance)
            uCrop.startEdit(fragment?.requireActivity() ?: return, fragment, requestCode)
        } catch (e: Exception) {
            Timber.e(e)
            showErrorToast("启动编辑错误：${e.message}")
        }
    }
}