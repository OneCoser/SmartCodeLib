package ch.smart.code.mvp.template.view.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import ch.smart.code.R
import ch.smart.code.imageloader.SCImageView
import ch.smart.code.imageloader.isStartsWithHttp
import ch.smart.code.imageloader.loadFileImage
import ch.smart.code.imageloader.loadImage
import ch.smart.code.mvp.BaseActivity
import ch.smart.code.mvp.IPresenter
import ch.smart.code.mvp.IView
import ch.smart.code.util.showErrorToast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import kotlinx.android.synthetic.main.public_activity_images.*

@Route(path = BasicImagesActivity.PATH)
class BasicImagesActivity : BaseActivity<IPresenter>(), IView {

    companion object {
        const val PATH = "/SmartCode/activity/sc_images"

        fun open(data: List<String>, index: Int = 0) {
            if (data.isNullOrEmpty()) return
            try {
                ARouter.getInstance().build(PATH)
                    .withObject("data", data)
                    .withInt("index", index)
                    .navigation()
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast(e.message ?: "无法打开,请检测配置")
            }
        }
    }

    @Autowired
    @JvmField
    var data: List<String>? = null

    @Autowired
    @JvmField
    var index: Int = 0

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
                val view = View.inflate(
                    container.context,
                    R.layout.public_item_image,
                    null
                ) as SCImageView
                if (view.parent == null) {
                    container.addView(view)
                }
                val url = data?.getOrNull(position)
                if (url?.isStartsWithHttp() == false) {
                    view.loadFileImage(url)
                } else {
                    view.loadImage(url)
                }
                return view
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
}