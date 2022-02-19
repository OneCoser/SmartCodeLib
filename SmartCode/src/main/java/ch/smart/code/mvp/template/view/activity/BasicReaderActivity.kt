package ch.smart.code.mvp.template.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import ch.smart.code.R
import ch.smart.code.bean.ShareBean
import ch.smart.code.imageloader.isStartsWithHttp
import ch.smart.code.mvp.BaseActivity
import ch.smart.code.mvp.IPresenter
import ch.smart.code.mvp.lifecycle.bindObservableToDestroyL
import ch.smart.code.network.HttpObserver
import ch.smart.code.util.*
import ch.smart.code.util.rx.SimpleObserver
import ch.smart.code.util.rx.toIoAndMain
import ch.smart.code.view.UIStatusView
import com.blankj.utilcode.util.ActivityUtils
import com.qmuiteam.qmui.widget.QMUITopBar
import com.tencent.smtt.sdk.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.public_activity_reader.*
import timber.log.Timber
import zlc.season.rxdownload4.download
import zlc.season.rxdownload4.task.Task
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 类描述：阅读器
 */
open class BasicReaderActivity : BaseActivity<IPresenter>() {

    companion object {
        const val FILE_PREFIX = "file://"
        fun getFilePath(file: File?): String? {
            return if (file?.exists() == true) {
                FILE_PREFIX + file.absolutePath
            } else null
        }

        fun open(
            path: String?,
            holdTitle: String? = null,
            holdUseReader: Boolean = false,
            shareParams: ShareBean? = null
        ) {
            if (path.isNullOrEmpty()) return
            try {
                ActivityUtils.startActivity(
                    Intent(ActivityUtils.getTopActivity(), BasicReaderActivity::class.java)
                        .putExtra("holdUseReader", holdUseReader)
                        .putExtra("holdTitle", holdTitle ?: "")
                        .putExtra("path", path)
                        .putExtra("shareParams", shareParams)
                )
            } catch (e: Exception) {
                Timber.e(e)
                showErrorToast(e.message ?: "无法打开,请检测配置")
            }
        }
    }

    open var holdUseReader: Boolean = false
    open var shareParams: ShareBean? = null
    open var holdTitle: String = ""
    open var path: String = ""
    open var cacheFile: File? = null

    private var readerView: View? = null
    private var download: Disposable? = null

    override fun createPresenter(): IPresenter? {
        return null
    }

    override fun initView(p0: Bundle?): Int {
        return R.layout.public_activity_reader
    }

    open fun getTopBar(): QMUITopBar {
        return publicTopBar
    }

    override fun initData(p0: Bundle?) {
        holdUseReader = intent?.getBooleanExtra("holdUseReader", false) ?: false
        shareParams = intent?.getParcelableExtra("shareParams")
        holdTitle = intent?.getStringExtra("holdTitle") ?: ""
        path = intent?.getStringExtra("path") ?: ""
        initReader()
    }

    open fun initReader() {
        if (holdTitle.isNotNullOrBlank()) {
            title = holdTitle
            getTopBar().setTitle(holdTitle)
        }
        checkNeedShare()
        readerStatus.click().subscribe(object : SimpleObserver<Unit>() {
            override fun onNext(t: Unit) {
                loadReader()
            }
        })
        loadReader()
    }

    open fun checkNeedShare() {
        if (shareParams != null) {
            getTopBar().removeAllRightViews()
            getTopBar().addRightTextButton(shareParams?.btName ?: "分享", R.id.public_title_right)
                .click {
                    openShare(cacheFile, path)
                }
        }
    }

    open fun openShare(file: File?, safePath: String) {
        try {
            val share = Share(this, this)
            val sharePath = if (file?.exists() == true) file.absolutePath else safePath
            val desc = shareParams?.desc ?: "分享文档"
            if (sharePath.isStartsWithHttp()) {
                share.share("$desc：$sharePath", "")
                return
            }
            if (sharePath.isNullOrBlank()) {
                showErrorToast("分享数据错误!")
                return
            }
            share.shareFiles(
                mapOf(sharePath to (shareParams?.holdName ?: "")),
                arrayListOf(sharePath.getMimeType(defaultType = "application/msword")),
                desc, ""
            )
        } catch (e: Exception) {
            Timber.e(e)
            showErrorToast("分享失败!")
        }
    }

    private fun loadReader() {
        readerStatus.showLoading()
        val isHttp = path.isStartsWithHttp()
        val isFile = path.startsWith(FILE_PREFIX)
        if (!isHttp && !isFile) {
            Timber.i("文件地址错误:%s", path)
            readerStatus.showError(msg = "文件地址错误!")
            return
        }
        disDownload()
        val ext = FileCache.getSuffix(path)
        val file = if (isFile) {
            File(path.replaceFirst(FILE_PREFIX, ""))
        } else {
            FileCache.getUrlFile(path, fixSuffix = ext)
        }
        Timber.i("加载文件：\n%s\n%s", path, file?.absolutePath)
        if (isFile || file == null || file.exists()) {
            showTbsReader(file, ext)
            return
        }

        download = Task(
            path.trim(),
            taskName = file.name,
            saveName = file.name,
            savePath = file.parent,
        ).download().toIoAndMain().subscribeBy(
            onNext = {
                Timber.i("下载%s：%s", it.percent(), path)
            },
            onComplete = {
                showTbsReader(file, ext)
                disDownload()
            },
            onError = {
                showTbsReader(null, null)
                disDownload()
            }
        )
    }

    private fun showTbsReader(file: File?, ext: String?) {
        cacheFile = file
        if (file?.exists() != true) {
            errorAction(file, ext, "文件加载失败!")
            return
        }
        try {
            val tbs = TbsReaderView(this,
                TbsReaderView.ReaderCallback { _, _, _ ->
                    Timber.i("TbsReaderView.ReaderCallback")
                }
            )
            addReaderViewToShow(tbs)
            if (tbs.preOpen(ext, false)) {
                tbs.openFile(Bundle().apply {
                    putString("filePath", file.absolutePath)
                    putString("tempPath", FileCache.getTempDir()?.absolutePath ?: "")
                })
                checkTitle(file.name)
                readerStatus.showDef()
            } else {
                errorAction(file, ext, "暂不支持此文件格式!")
            }
        } catch (e: Exception) {
            Timber.e(e)
            errorAction(file, ext, String.format("加载失败：%s", e.message))
        }
    }

    private fun errorAction(file: File?, ext: String?, msg: String) {
        if (file?.exists() == true) {
            Timber.i("加载失败，但文件存在，使用QbSdk打开：%s", file.absolutePath)
            openQbFileReader(file.absolutePath)
        } else if (!holdUseReader && path.isStartsWithHttp()) {
            Timber.i("加载失败，尝试用WebView展示：%s", path)
            addReaderViewToShow(WebView(this).apply {
                this.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        view.loadUrl(url)
                        return true
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        checkTitle(view.title)
                    }
                }
                this.initSetting()
                this.loadUrl(path.trim())
            })
            readerStatus.showDef()
        } else {
            readerStatus.showError(msg = msg)
        }
    }

    private fun openQbFileReader(filePath: String) {
        Observable.just(filePath).delay(1, TimeUnit.SECONDS)
            .bindObservableToDestroyL(this).toIoAndMain()
            .subscribe(object : HttpObserver<String>() {
                override fun onNext(t: String) {
                    readerStatus.showDef()
                    QbSdk.openFileReader(this@BasicReaderActivity, t, null, ValueCallback<String> {
                        Timber.i("QbSdk.openFileReader：%s", it)
                        readerStatus.showError(msg = it)
                        if (it == "fileReaderClosed") {
                            this@BasicReaderActivity.finish()
                        }
                    })
                }

                override fun onError(throwable: Throwable) {
                    super.onError(throwable)
                    readerStatus.showError(msg = throwable.toString())
                }
            })
    }

    private fun checkTitle(title: String?) {
        if (holdTitle.isBlank() && title.isNotNullOrBlank()) {
            publicTopBar.setTitle(title)
        }
    }

    private fun disDownload() {
        download?.dispose()
        download = null
    }

    open fun getReaderLay(): ViewGroup {
        return readerLay
    }

    open fun addReaderViewToShow(view: View) {
        readerView?.let {
            destroyReaderView(it)
            getReaderLay().removeView(it)
        }
        readerView = view
        getReaderLay().addView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    open fun getReaderView(): View? {
        return readerView
    }

    open fun getReaderStatusView(): UIStatusView {
        return readerStatus
    }

    private fun destroyReaderView(view: View?) {
        try {
            when (view) {
                is WebView -> view.destroy()
                is TbsReaderView -> view.onStop()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onBackPressed() {
        val v = getReaderView()
        if (v is WebView && v.canGoBack()) {
            v.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        disDownload()
        destroyReaderView(getReaderView())
        super.onDestroy()
    }
}