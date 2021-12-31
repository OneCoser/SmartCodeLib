package ch.smart.code.mvp.template.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import ch.smart.code.R
import ch.smart.code.bean.ShareBean
import ch.smart.code.imageloader.*
import ch.smart.code.mvp.BaseActivity
import ch.smart.code.mvp.IPresenter
import ch.smart.code.mvp.IView
import ch.smart.code.util.*
import ch.smart.code.util.rx.toIoAndMain
import ch.smart.code.view.SCProgressBar
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.Utils
import com.github.piasy.biv.indicator.ProgressIndicator
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.FrescoImageViewFactory
import com.qmuiteam.qmui.widget.QMUITopBar
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.public_activity_images.*
import timber.log.Timber
import zlc.season.rxdownload4.download
import zlc.season.rxdownload4.task.Task

open class BasicImagesActivity : BaseActivity<IPresenter>(), IView {

    companion object {
        fun open(
            data: ArrayList<String>,
            index: Int = 0,
            needBig: Boolean = true,
            shareParams: ShareBean? = null
        ) {
            if (data.isNullOrEmpty()) return
            try {
                ActivityUtils.startActivity(
                    Intent(ActivityUtils.getTopActivity(), BasicImagesActivity::class.java)
                        .putStringArrayListExtra("data", data)
                        .putExtra("index", index)
                        .putExtra("needBig", needBig)
                        .putExtra("shareParams", shareParams)
                )
            } catch (e: Exception) {
                Timber.e(e)
                showErrorToast(e.message ?: "无法打开,请检测配置")
            }
        }
    }

    open var data: ArrayList<String>? = null
    open var index: Int = 0
    open var needBig: Boolean = true
    open var shareParams: ShareBean? = null
    private var download: Disposable? = null

    override fun createPresenter(): IPresenter? {
        return null
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.public_activity_images
    }

    open fun getTopBar(): QMUITopBar {
        return publicTopBar
    }

    override fun initData(savedInstanceState: Bundle?) {
        data = intent?.getStringArrayListExtra("data")
        index = intent?.getIntExtra("index", 0) ?: 0
        needBig = intent?.getBooleanExtra("needBig", true) ?: true
        shareParams = intent?.getParcelableExtra("shareParams")
        checkNeedShare()
        initPager()
    }

    open fun initPager() {
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

    open fun checkNeedShare() {
        if (shareParams != null) {
            getTopBar().removeAllRightViews()
            getTopBar().addRightTextButton(shareParams?.btName ?: "分享", R.id.public_title_right)
                .click {
                    openShare(data?.getOrNull(imagePager.currentItem))
                }
        }
    }

    open fun openShare(path: String?) {
        try {
            disDownload()
            if (path.isNullOrBlank()) {
                showErrorToast("分享数据错误!")
                return
            }
            if (path.isStartsWithHttp()) {
                val file = FileCache.getUrlFile(url = path)
                when {
                    file?.exists() == true -> openShare(file.absolutePath)
                    file != null -> {
                        showLoading()
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
                                openShare(file.absolutePath)
                            },
                            onError = {
                                disDownload()
                                showErrorToast("下载分享数据失败!")
                            }
                        )
                    }
                    else -> showErrorToast("获取分享数据失败!")
                }
                return
            }
            Share(this, this).shareFiles(
                mapOf(path to (shareParams?.holdName ?: "")),
                arrayListOf(path.getMimeType()),
                shareParams?.desc ?: "分享图片", ""
            )
        } catch (e: Exception) {
            Timber.e(e)
            showErrorToast("分享失败!")
        }
    }

    open fun disDownload() {
        download?.dispose()
        download = null
        dismissLoading()
    }

    open fun createSimpleImage(container: ViewGroup, url: String?): View {
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

    open fun createBigImage(container: ViewGroup, url: String?): View {
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

    override fun onDestroy() {
        disDownload()
        super.onDestroy()
    }
}