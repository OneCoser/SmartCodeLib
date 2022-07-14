package ch.smart.code.util.selector

import android.net.Uri
import androidx.fragment.app.Fragment
import ch.smart.code.util.createUCropOptions
import ch.smart.code.util.showErrorToast
import com.luck.picture.lib.engine.CropFileEngine
import com.luck.picture.lib.style.PictureSelectorStyle
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.util.ArrayList

open class CropImageEngine(
    private val style: PictureSelectorStyle? = null
) : CropFileEngine {

    override fun onStartCrop(
        fragment: Fragment?,
        srcUri: Uri?,
        destinationUri: Uri?,
        dataSource: ArrayList<String>?,
        requestCode: Int
    ) {
        try {
            val uCrop = UCrop.of<Any>(srcUri ?: return, destinationUri ?: return)
            val options = createUCropOptions(style = style)
            options.setHideBottomControls(false)
            uCrop.withOptions(options)
            uCrop.setImageEngine(GlideUCropImageEngine.instance)
            uCrop.startEdit(fragment?.requireActivity() ?: return, fragment, requestCode)
        } catch (e: Exception) {
            Timber.e(e)
            showErrorToast("启动裁剪错误：${e.message}")
        }
    }
}