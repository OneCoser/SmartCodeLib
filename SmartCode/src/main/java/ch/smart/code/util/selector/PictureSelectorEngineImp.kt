package ch.smart.code.util.selector

import ch.smart.code.R
import ch.smart.code.util.safeToJson
import com.luck.picture.lib.basic.IBridgeLoaderFactory
import com.luck.picture.lib.config.InjectResourceSource
import com.luck.picture.lib.engine.*
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnInjectLayoutResourceListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import timber.log.Timber

//这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致各种Engine被回收
class PictureSelectorEngineImp : PictureSelectorEngine {
    override fun createImageLoaderEngine(): ImageEngine {
        return GlideImageEngine.instance
    }

    override fun createCompressEngine(): CompressEngine? {
        return null
    }

    override fun createCompressFileEngine(): CompressFileEngine? {
        return CompressImageEngine()
    }

    override fun createLoaderDataEngine(): ExtendLoaderEngine? {
        return null
    }

    override fun createVideoPlayerEngine(): VideoPlayerEngine<*>? {
        return null
    }

    override fun onCreateLoader(): IBridgeLoaderFactory? {
        return null
    }

    override fun createSandboxFileEngine(): SandboxFileEngine? {
        return null
    }

    override fun createUriToFileTransformEngine(): UriToFileTransformEngine? {
        return null
    }

    override fun createLayoutResourceListener(): OnInjectLayoutResourceListener? {
        return OnInjectLayoutResourceListener { _, resourceSource ->
            when (resourceSource) {
                InjectResourceSource.MAIN_SELECTOR_LAYOUT_RESOURCE -> R.layout.ps_fragment_selector
                InjectResourceSource.PREVIEW_LAYOUT_RESOURCE -> R.layout.ps_fragment_preview
                InjectResourceSource.MAIN_ITEM_IMAGE_LAYOUT_RESOURCE -> R.layout.ps_item_grid_image
                InjectResourceSource.MAIN_ITEM_VIDEO_LAYOUT_RESOURCE -> R.layout.ps_item_grid_video
                InjectResourceSource.MAIN_ITEM_AUDIO_LAYOUT_RESOURCE -> R.layout.ps_item_grid_audio
                InjectResourceSource.ALBUM_ITEM_LAYOUT_RESOURCE -> R.layout.ps_album_folder_item
                InjectResourceSource.PREVIEW_ITEM_IMAGE_LAYOUT_RESOURCE -> R.layout.ps_preview_image
                InjectResourceSource.PREVIEW_ITEM_VIDEO_LAYOUT_RESOURCE -> R.layout.ps_preview_video
                InjectResourceSource.PREVIEW_GALLERY_ITEM_LAYOUT_RESOURCE -> R.layout.ps_preview_gallery_item
                else -> 0
            }
        }
    }

    override fun getResultCallbackListener(): OnResultCallbackListener<LocalMedia?>? {
        return object : OnResultCallbackListener<LocalMedia?> {
            override fun onResult(result: ArrayList<LocalMedia?>) {
                Timber.d("选择文件结果：%s", safeToJson(result))
            }

            override fun onCancel() {
                Timber.d("选择文件取消")
            }
        }
    }
}