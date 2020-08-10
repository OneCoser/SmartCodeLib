package ch.smart.code.util

import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.engine.PictureSelectorEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import ch.smart.code.util.json
import timber.log.Timber

class PictureSelectorEngineImp : PictureSelectorEngine {
    
    override fun getResultCallbackListener(): OnResultCallbackListener<LocalMedia> {
        return object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: MutableList<LocalMedia>?) {
                Timber.i("返回选择的图片：%s", if (result.isNullOrEmpty()) "null" else json.toJson(result))
            }
            
            override fun onCancel() {
                Timber.i("取消图片选择")
            }
        }
    }
    
    override fun createEngine(): ImageEngine {
        return GlideImageEngine.getInstance() ?: GlideImageEngine()
    }
}