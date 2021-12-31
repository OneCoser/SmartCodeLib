package ch.smart.code.mvp.template.view.activity

import android.content.Intent
import android.os.Bundle
import ch.smart.code.R
import ch.smart.code.bean.ShareBean
import ch.smart.code.imageloader.isStartsWithHttp
import ch.smart.code.mvp.BaseActivity
import ch.smart.code.mvp.IPresenter
import ch.smart.code.util.*
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import kotlinx.android.synthetic.main.public_activity_web.*
import kotlinx.android.synthetic.main.public_title.*
import com.blankj.utilcode.util.ActivityUtils
import com.qmuiteam.qmui.widget.QMUITopBar
import timber.log.Timber

/**
 * 类描述：网页浏览器
 */
open class BasicWebActivity : BaseActivity<IPresenter>() {

    companion object {
        fun open(
            url: String?,
            holdTitle: String? = null,
            shareParams: ShareBean? = null
        ) {
            if (url?.isStartsWithHttp() != true) return
            try {
                ActivityUtils.startActivity(
                    Intent(ActivityUtils.getTopActivity(), BasicWebActivity::class.java)
                        .putExtra("baseUrl", url)
                        .putExtra("holdTitle", holdTitle ?: "")
                        .putExtra("shareParams", shareParams)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast(e.message ?: "无法打开,请检测配置")
            }
        }
    }

    open var baseUrl: String = ""
    open var cacheUrl: String? = null
    open var holdTitle: String = ""
    open var shareParams: ShareBean? = null

    override fun createPresenter(): IPresenter? {
        return null
    }

    override fun initView(p0: Bundle?): Int {
        return R.layout.public_activity_web
    }

    open fun getTopBar(): QMUITopBar {
        return publicTopBar
    }

    override fun initData(p0: Bundle?) {
        shareParams = intent?.getParcelableExtra("shareParams")
        holdTitle = intent?.getStringExtra("holdTitle") ?: ""
        baseUrl = intent?.getStringExtra("baseUrl") ?: ""
        if (holdTitle.isNotNullOrBlank()) {
            title = holdTitle
            getTopBar().setTitle(holdTitle)
        }
        checkNeedShare()
        initWeb()
    }

    open fun checkNeedShare() {
        if (shareParams != null) {
            getTopBar().removeAllRightViews()
            getTopBar().addRightTextButton(shareParams?.holdName ?: "分享", R.id.public_title_right)
                .click {
                    openShare(
                        if (cacheUrl?.isStartsWithHttp() == true) {
                            cacheUrl ?: baseUrl
                        } else baseUrl
                    )
                }
        }
    }

    open fun openShare(url: String) {
        try {
            Share(this, this)
                .share("${shareParams?.desc ?: "分享网页"}：$url", "")
        } catch (e: Exception) {
            Timber.e(e)
            showErrorToast("分享失败!")
        }
    }

    open fun initWeb() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                cacheUrl = url
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (holdTitle.isBlank() && view.title.isNotNullOrBlank()) {
                    getTopBar().setTitle(view.title)
                }
            }
        }
        webView.initSetting()
        webView.loadUrl(baseUrl)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}