package ch.smart.code.util.selector

import android.content.Context
import android.net.Uri
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.util.ArrayList

open class CompressImageEngine : CompressFileEngine {

    override fun onStartCompress(
        context: Context?,
        source: ArrayList<Uri>?,
        call: OnKeyValueResultCallbackListener?
    ) {
        if (source.isNullOrEmpty()) {
            return
        }
        Luban.with(context).load(source).ignoreBy(100)
            .setCompressListener(object : OnNewCompressListener {
                override fun onStart() {
                }

                override fun onSuccess(key: String?, compressFile: File?) {
                    call?.onCallback(key, compressFile?.absolutePath)
                }

                override fun onError(key: String?, e: Throwable?) {
                    call?.onCallback(key, null)
                }
            }).launch()
    }
}