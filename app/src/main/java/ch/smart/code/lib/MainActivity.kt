package ch.smart.code.lib

import android.app.Activity
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import ch.smart.code.adapter.StatusBarAdapter
import ch.smart.code.bean.ShareBean
import ch.smart.code.mvp.template.view.activity.BasicReaderActivity
import ch.smart.code.dialog.ItemAlert
import ch.smart.code.mvp.template.view.activity.BasicImagesActivity
import ch.smart.code.mvp.template.view.activity.BasicWebActivity
import ch.smart.code.network.HttpObserver
import ch.smart.code.util.*
import ch.smart.code.util.rx.toIoAndMain
import com.blankj.utilcode.util.ActivityUtils
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : Activity(), StatusBarAdapter {
    private val selectListener by lazy {
        object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: MutableList<LocalMedia>?) {
                Timber.i("选择结果：%s", safeToJson(result))
            }

            override fun onCancel() {
                showToast("取消选择")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testReader.click {
            ItemAlert(this)
                .setListener(object : ItemAlert.ItemAlertClickListener {
                    override fun onClick(alert: ItemAlert, itemIndex: Int, itemTag: Any?) {
                        alert.cancel()
                        if (itemIndex == 0) {
                            BasicWebActivity.open(
                                url = itemTag?.toString(),
                                shareParams = ShareBean()
                            )
                        } else {
                            BasicReaderActivity.open(
                                path = itemTag?.toString(),
                                holdUseReader = false,
                                shareParams = ShareBean(holdName = "测试文档")
                            )
                        }
                    }
                })
                .addItem("网页", tag = "https://www.baidu.com")
                .addItem(
                    "文件",
                    tag = "https://tanren.oss-cn-shenzhen.aliyuncs.com/patient/规章制度/行业规范1636601626326-88453-陈先达《马克思主义哲学原理》（第5版）笔记和课后习题（含考研真题）详解.pdf"
                )
                .addItem("错误地址", tag = "12344343")
                .setCanceledOnTouchOutsideS(true).setCancelableS(true).show()
        }
        testApi.click {
            apiService.loadList(mapOf("userId" to "2001"))
                .toIoAndMain()
                .doOnSubscribe {
                    showLoading(ActivityUtils.getTopActivity())
                }
                .doFinally {
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
        testSelectImage.click {
            openImageSelect(this, selectListener)
        }
        testSelectVideo.click {
            openVideoSelect(this, selectListener)
        }
        testPermission.click {
            requestCameraAndStorage(this) {
                showToast("获取成功!")
            }
        }
        testBigImage.click {
            BasicImagesActivity.open(
                arrayListOf(
                    "https://tanren.oss-cn-shenzhen.aliyuncs.com/patient/贵阳供电局2021年电网建设10千伏及以下项目/贵阳供电局2021年电网建设10千伏及以下项目（开阳供电局）/开工报告/1637809538984-30743-开工令.jpg"
                ),
                shareParams = ShareBean(holdName = "测试图片")
            )
        }
        testVersion.click {
            VersionCheck(
                apiKey = "b5d9694646f2be460fd0da6504ebb7cd",
                appKey = "b3629ce862ad7dbfacfee203f5d311b5",
                nowVersionCode = BuildConfig.VERSION_CODE,
                nowVersionName = BuildConfig.VERSION_NAME
            ).start(beQuiet = false)
        }
        requestForStartup(this) {
            Timber.i("初始化权限成功")
        }
    }

    override fun getBarColor(): Int {
        return Color.TRANSPARENT
    }
}