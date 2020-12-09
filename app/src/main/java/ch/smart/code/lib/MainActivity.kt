package ch.smart.code.lib

import android.app.Activity
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import ch.smart.code.adapter.StatusBarAdapter
import ch.smart.code.base.SCReaderActivity
import ch.smart.code.base.SCWebActivity
import ch.smart.code.dialog.ItemAlert
import ch.smart.code.network.HttpObserver
import ch.smart.code.util.*
import ch.smart.code.util.rx.toIoAndMain
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.Utils
import com.jess.arms.utils.ArmsUtils
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : Activity(), StatusBarAdapter {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testReader.click {
            ItemAlert(
                this,
                object : ItemAlert.ItemAlertClickListener {
                    override fun onClick(alert: ItemAlert, itemIndex: Int, itemTag: Any?) {
                        alert.cancel()
                        SCReaderActivity.open(path = itemTag?.toString())
                    }
                }
            ).addItem("网页", tag = "https://www.baidu.com")
                .addItem(
                    "文件",
                    tag = "http://center.voniu.com/api/m/v1/file/get?path=reportpdf/6bbccd16-9ad9-4d5d-8fc5-b880fdd59120.pdf&token=164e6594-7492-45d9-902e-1873aa9f9d65"
                )
                .addItem("错误地址", tag = "12344343")
                .setCanceledOnTouchOutsideS(true).setCancelableS(true).show()
        }
        testApi.click {
            ArmsUtils.obtainAppComponentFromContext(Utils.getApp())
                .repositoryManager()
                .obtainRetrofitService(ApiService::class.java)
                .loadList(mapOf("userId" to "2001"))
                .toIoAndMain()
                .doOnSubscribe {
                    showLoading(ActivityUtils.getTopActivity(), cancelable = false)
                }
                .doOnComplete {
                    dismissLoading()
                }.subscribe(object : HttpObserver<List<String>>() {
                    override fun onNext(t: List<String>) {
                        showSuccessToast("请求成功!")
                    }
                })
        }
        testRing.click {
            RingtonePlayer(repeat = false).start(type = RingtoneManager.TYPE_NOTIFICATION)
        }
        requestForStartup(this) {
            Timber.i("初始化权限成功")
        }
    }

    override fun getBarColor(): Int {
        return Color.TRANSPARENT
    }
}