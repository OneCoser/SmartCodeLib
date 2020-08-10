package ch.smart.code.util

import ch.smart.code.dialog.VersionAlert
import ch.smart.code.imageloader.isStartsWithHttp
import com.blankj.utilcode.util.ActivityUtils
import com.google.gson.JsonParser
import io.reactivex.Observable
import okhttp3.ResponseBody
import ch.smart.code.util.isNotNullOrBlank
import ch.smart.code.util.rx.SimpleObserver
import ch.smart.code.util.rx.toIoAndMain
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import timber.log.Timber

class VersionCheck(
    private val apiKey: String,
    private val appKey: String,
    private val nowVersionCode: Int,
    private val nowVersionName: String,
    private val buildVersionNum: Int = 0
) {

    interface Api {

        @Headers("Content-type:application/x-www-form-urlencoded;charset=UTF-8")
        @FormUrlEncoded
        @POST("check")
        fun checkVersion(
            @Field("_api_key") apiKey: String,
            @Field("appKey") appKey: String,
            @Field("buildVersion") buildVersion: String,
            @Field("buildBuildVersion") buildBuildVersion: Int
        ): Call<ResponseBody>

    }

    private val api by lazy {
        Retrofit.Builder().baseUrl("https://www.pgyer.com/apiv2/app/")
            .client(getOkHttpBuilder().build())
            .build().create(VersionCheck.Api::class.java)
    }

    private var beQuiet: Boolean = false

    /**
     * buildBuildVersion	Integer	蒲公英生成的用于区分历史版本的build号
     * forceUpdateVersion	String	强制更新版本号（未设置强置更新默认为空）
     * forceUpdateVersionNo	String	强制更新的版本编号
     * needForceUpdate	Boolean	是否强制更新
     * downloadURL	String	应用安装地址
     * buildHaveNewVersion	Boolean	是否有新版本
     * buildVersionNo	String	上传包的版本编号，默认为1 (即编译的版本号，一般来说，编译一次会变动一次这个版本号, 在 Android 上叫 Version Code。对于 iOS 来说，是字符串类型；对于 Android 来说是一个整数。例如：1001，28等。)
     * buildVersion	String	版本号, 默认为1.0 (是应用向用户宣传时候用到的标识，例如：1.1、8.2.1等。)
     * buildShortcutUrl	String	应用短链接
     * buildUpdateDescription	String	应用更新说明
     * **/
    fun start(beQuiet: Boolean = false) {
        this.beQuiet = beQuiet
        if (!checkInstallAlert(ActivityUtils.getTopActivity())) {
            return
        }
        try {
            if (!beQuiet) showLoading(ActivityUtils.getTopActivity(), cancelable = false)
            api.checkVersion(apiKey, appKey, nowVersionName, buildVersionNum)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Timber.e(t)
                        showError()
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Observable.just(response.body()?.string() ?: "")
                            .map {
                                Timber.i(it)
                                val map = mutableMapOf<String, String>()
                                try {
                                    val data =
                                        JsonParser.parseString(it).asJsonObject?.getAsJsonObject("data")
                                    val versionNo = data?.get("buildVersionNo")?.asInt ?: 0
                                    val url = data?.get("downloadURL")?.asString
                                    val versionBB = data?.get("buildBuildVersion")?.asInt ?: 0
                                    if ((versionNo > nowVersionCode || buildVersionNum in 1 until versionBB)
                                        && url?.isStartsWithHttp() == true
                                    ) {
                                        map["url"] = url
                                        map["name"] = data.get("buildVersion")?.asString ?: ""
                                        map["desc"] =
                                            data.get("buildUpdateDescription")?.asString ?: ""
                                        if (data.get("needForceUpdate")?.asBoolean == true) {
                                            map["force"] = "true"
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e)
                                }
                                map
                            }.toIoAndMain()
                            .subscribe(object : SimpleObserver<Map<String, String>>() {
                                override fun onNext(t: Map<String, String>) {
                                    dismissLoading()
                                    val name = t["name"]
                                    val url = t["url"]
                                    if (name.isNullOrBlank() || url.isNullOrBlank()) {
                                        if (!beQuiet) showToast("当前已是最新版本!")
                                        return
                                    }
                                    VersionAlert(
                                        context = ActivityUtils.getTopActivity(),
                                        name = name,
                                        desc = t["desc"],
                                        force = t["force"].isNotNullOrBlank(),
                                        url = url
                                    ).show()
                                }
                            })
                    }
                })
        } catch (e: Exception) {
            Timber.e(e)
            showError()
        }
    }

    private fun showError() {
        if (beQuiet) return
        ActivityUtils.getTopActivity()?.runOnUiThread {
            dismissLoading()
            showErrorToast("检测更新失败!")
        }
    }
}