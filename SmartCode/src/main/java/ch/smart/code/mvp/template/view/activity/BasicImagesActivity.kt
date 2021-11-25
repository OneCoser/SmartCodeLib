package ch.smart.code.mvp.template.view.activity

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import ch.smart.code.R
import ch.smart.code.imageloader.*
import ch.smart.code.mvp.BaseActivity
import ch.smart.code.mvp.IPresenter
import ch.smart.code.mvp.IView
import ch.smart.code.util.drawable
import ch.smart.code.util.pt
import ch.smart.code.util.showErrorToast
import ch.smart.code.view.SCProgressBar
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.Utils
import com.github.piasy.biv.indicator.ProgressIndicator
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.FrescoImageViewFactory
import kotlinx.android.synthetic.main.public_activity_images.*
import timber.log.Timber

@Route(path = BasicImagesActivity.PATH)
class BasicImagesActivity : BaseActivity<IPresenter>(), IView {

    companion object {
        const val PATH = "/SmartCode/activity/sc_images"

        fun open(data: ArrayList<String>, index: Int = 0, needBig: Boolean = true) {
            if (data.isNullOrEmpty()) return
            try {
                ARouter.getInstance().build(PATH)
                    .withStringArrayList("data", data)
                    .withInt("index", index)
                    .withBoolean("needBig", needBig)
                    .navigation()
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast(e.message ?: "无法打开,请检测配置")
            }
        }
    }

    @Autowired
    @JvmField
    var data: ArrayList<String>? = null

    @Autowired
    @JvmField
    var index: Int = 0

    @Autowired
    @JvmField
    var needBig: Boolean = true

    override fun createPresenter(): IPresenter? {
        return null
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.public_activity_images
    }

    override fun initData(savedInstanceState: Bundle?) {
        imagePager.adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return data?.size ?: 0
            }

            override fun getItemPosition(`object`: Any): Int {
                return POSITION_NONE
            }

            override fun isViewFromObject(view: View, item: Any): Boolean {
                return view == item
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                return if (needBig) {
                    createBigImage(container, data?.getOrNull(position))
                } else {
                    createSimpleImage(container, data?.getOrNull(position))
                }
            }

            override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
                if (item is View) {
                    container.removeView(item)
                }
            }
        }
        val size = data?.size ?: 0
        imagePager.currentItem = if (index in 0 until size) {
            index
        } else {
            0
        }
    }

    private fun createSimpleImage(container: ViewGroup, url: String?): View {
        val view = View.inflate(
            container.context,
            R.layout.public_item_image,
            null
        ) as SCImageView
        if (view.parent == null) {
            container.addView(view)
        }
        if (url?.isStartsWithHttp() == false) {
            view.loadFileImage(url)
        } else {
            view.loadImage(url)
        }
        return view
    }

    private fun createBigImage(container: ViewGroup, url: String?): View {
        val view = BigImageView(container.context)
        view.setImageViewFactory(FrescoImageViewFactory())
        view.setFailureImage(R.drawable.public_error_img.drawable())
        view.setFailureImageInitScaleType(ImageView.ScaleType.CENTER)
        view.setProgressIndicator(object : ProgressIndicator {
            var progressView: SCProgressBar? = null
            override fun onFinish() {
                progressView?.visibility = View.GONE
            }

            override fun getView(parent: BigImageView?): View {
                val context = parent?.context ?: Utils.getApp()
                progressView = View.inflate(
                    context,
                    R.layout.public_view_scprogress,
                    null
                ) as SCProgressBar
                val size = 32.pt
                val params = FrameLayout.LayoutParams(size, size)
                params.gravity = Gravity.CENTER
                progressView?.layoutParams = params
                return progressView!!
            }

            override fun onProgress(progress: Int) {
                progressView?.setProgress(progress, false)
            }

            override fun onStart() {
                progressView?.visibility = View.VISIBLE
            }
        })
        container.addView(view)
        Timber.i("LoadBigImage: %s", url)
        view.showImage(
            Uri.parse(
                when {
                    url.isNullOrBlank() -> ""
                    url.startsWith("/") -> "file://$url"
                    else -> url
                }
            )
        )
        return view
    }
}