package ch.smart.code.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import ch.smart.code.R
import ch.smart.code.util.FileCache
import ch.smart.code.util.initSetting
import ch.smart.code.util.showErrorToast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.jess.arms.mvp.IPresenter
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import ch.smart.code.util.isNotNullOrBlank
import ch.smart.code.util.rx.SimpleObserver
import ch.smart.code.util.rx.toIoAndMain
import com.tencent.smtt.sdk.TbsReaderView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.public_activity_reader.*
import timber.log.Timber
import zlc.season.rxdownload3.RxDownload
import zlc.season.rxdownload3.core.Failed
import zlc.season.rxdownload3.core.Mission
import zlc.season.rxdownload3.core.Succeed
import java.io.File

/**
 * 类描述：
 * 创建人：chenhao
 * 创建时间：2020/7/23 21:03
 */
@Route(path = SCReaderActivity.PATH)
open class SCReaderActivity : SCBaseActivity<IPresenter>() {

    companion object {
        const val PATH = "/SmartCode/activity/sc_reader"
        fun open(holdTitle: String? = null, path: String?) {
            if (path.isNullOrEmpty()) return
            try {
                ARouter.getInstance().build(PATH)
                    .withString("holdTitle", holdTitle ?: "")
                    .withString("path", path)
                    .navigation()
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast(e.message ?: "无法打开,请检测配置")
            }
        }
    }

    @Autowired
    @JvmField
    var holdTitle: String = ""

    @Autowired
    @JvmField
    var path: String = ""

    private var readerView: View? = null
    private var download: Disposable? = null

    override fun initView(p0: Bundle?): Int {
        return R.layout.public_activity_reader
    }

    override fun initData(p0: Bundle?) {
        publicTopBar.setTitle(holdTitle)
        readerStatus.click().subscribe(object : SimpleObserver<Unit>() {
            override fun onNext(t: Unit) {
                loadReader()
            }
        })
        loadReader()
    }

    private fun loadReader() {
        readerStatus.showLoading()
        val ext = FileCache.getSuffix(path)
        if (ext.isNullOrEmpty() || !TbsReaderView.isSupportExt(this, ext)) {
            Timber.i("加载网页：%s \n %s", path, ext)
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
                this.loadUrl(path)
            })
            readerStatus.showDef()
            return
        }
        disDownload()
        val file = FileCache.getUrlFile(path, fixSuffix = ext)
        Timber.i("加载文件：%s \n %s", path, file?.absolutePath)
        if (file == null || file.exists()) {
            showTbsReader(file, ext)
            return
        }
        download = RxDownload.create(
            Mission(path, file.name, file.parent, overwrite = true, enableNotification = false),
            true
        ).toIoAndMain().subscribe { status ->
            if (status is Succeed) {
                showTbsReader(file, ext)
                disDownload()
            } else if (status is Failed) {
                showTbsReader(null, null)
                disDownload()
            }
        }
    }

    private fun showTbsReader(file: File?, ext: String?) {
        if (file?.exists() != true) {
            readerStatus.showError(msg = "加载失败!")
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
                readerStatus.showError(msg = "暂不支持此文件格式!")
            }
        } catch (e: Exception) {
            Timber.e(e)
            readerStatus.showError(msg = String.format("加载失败：%s", e.message))
        }
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