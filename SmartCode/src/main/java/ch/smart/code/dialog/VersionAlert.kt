package ch.smart.code.dialog

import android.content.Context
import ch.smart.code.R
import ch.smart.code.util.FileCache
import ch.smart.code.util.rx.toIoAndMain
import ch.smart.code.util.showErrorToast
import com.blankj.utilcode.util.AppUtils
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import zlc.season.rxdownload4.download
import zlc.season.rxdownload4.task.Task
import java.io.File

class VersionAlert(
    context: Context,
    private val name: String,
    private val desc: String?,
    private val force: Boolean,
    private val url: String
) : ItemMsgAlert(context), ItemMsgAlert.ItemMsgAlertClickListener {

    private var download: Disposable? = null

    override fun show() {
        setListener(this)
        setCancelableS(false)
        setCanceledOnTouchOutsideS(false)
        setTitle(String.format("新版本：V%s", name))
        setMsg(if (desc.isNullOrBlank()) "优化性能，提升交互体验，建议尽快更新" else desc)
        addItem("下载更新", tag = "submit", txtColorId = R.color.public_color_0F7FD6)
        if (!force) {
            addItem("取消")
        }
        setOnDismissListener { disDownload() }
        super.show()
    }

    override fun onClick(alert: ItemMsgAlert, itemIndex: Int, itemTag: Any?) {
        if (itemTag == null) {
            cancel()
            return
        }
        this.showLoading("下载更新中..")
        disDownload()
        val file = FileCache.getUrlFile(url)
        if (file == null || file.exists()) {
            actionDownload(file)
            return
        }
        Timber.i("开始下载%s：%s", name, url)
        download = Task(
            url,
            taskName = file.name,
            saveName = file.name,
            savePath = file.parent ?: ""
        ).download().toIoAndMain().subscribeBy(
            onNext = {
                Timber.i("下载更新%s：%s", it.percent(), url)
            },
            onComplete = {
                disDownload()
                actionDownload(file)
            },
            onError = {
                disDownload()
                actionDownload(file)
            }
        )
    }

    private fun disDownload() {
        download?.dispose()
        download = null
    }

    private fun actionDownload(file: File?) {
        this.hideLoading()
        if (file?.exists() != true) {
            actionError()
            return
        }
        try {
//            val activity = ActivityUtils.getTopActivity()
//            activity.startActivity(
//                Intent(Intent.ACTION_VIEW).apply {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        setDataAndType(
//                            FileProvider.getUriForFile(
//                                activity,
//                                String.format("%s.fileProvider", activity.packageName),
//                                file
//                            ), "application/vnd.android.package-archive"
//                        )
//                    } else {
//                        setDataAndType(
//                            Uri.fromFile(file),
//                            "application/vnd.android.package-archive"
//                        )
//                    }
//                }
//            )
            AppUtils.installApp(file)
            cancel()
        } catch (e: Exception) {
            Timber.e(e)
            actionError()
        }
    }

    private fun actionError() {
        showErrorToast("安装更新失败！")
        if (force) {
            cancel()
        }
    }
}