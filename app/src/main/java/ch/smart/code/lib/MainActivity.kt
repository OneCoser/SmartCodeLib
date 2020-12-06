package ch.smart.code.lib

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import ch.smart.code.adapter.StatusBarAdapter
import ch.smart.code.base.SCReaderActivity
import ch.smart.code.base.SCWebActivity
import ch.smart.code.dialog.ItemAlert
import ch.smart.code.util.click
import ch.smart.code.util.requestForStartup
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : Activity(), StatusBarAdapter {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testClick.click {
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
                .setCanceledOnTouchOutsideS(true).setCancelableS(true).show()
        }
        requestForStartup(this) {
            Timber.i("初始化权限成功")
        }
    }

    override fun getBarColor(): Int {
        return Color.TRANSPARENT
    }
}