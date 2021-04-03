package ch.smart.code.mvp.template.view.activity

import android.os.Bundle
import ch.smart.code.R
import ch.smart.code.imageloader.isStartsWithHttp
import ch.smart.code.mvp.BaseActivity
import ch.smart.code.mvp.IPresenter
import ch.smart.code.util.initSetting
import ch.smart.code.util.showErrorToast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import kotlinx.android.synthetic.main.public_activity_web.*
import kotlinx.android.synthetic.main.public_title.*
import ch.smart.code.util.isNotNullOrBlank

/**
 * 类描述：网页浏览器
 */
@Route(path = BasicWebActivity.PATH)
open class BasicWebActivity : BaseActivity<IPresenter>() {

    companion object {
        const val PATH = "/SmartCode/activity/sc_web"
        fun open(holdTitle: String? = null, url: String) {
            if (!url.isStartsWithHttp()) return
            try {
                ARouter.getInstance().build(PATH)
                    .withString("holdTitle", holdTitle ?: "")
                    .withString("baseUrl", url)
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
    var baseUrl: String = ""

    override fun createPresenter(): IPresenter? {
        return null
    }

    override fun initView(p0: Bundle?): Int {
        return R.layout.public_activity_web
    }

    override fun initData(p0: Bundle?) {
        publicTopBar.setTitle(holdTitle)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (holdTitle.isBlank() && view.title.isNotNullOrBlank()) {
                    publicTopBar.setTitle(view.title)
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