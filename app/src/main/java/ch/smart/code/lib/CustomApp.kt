package ch.smart.code.lib

import ch.smart.code.SmartCodeApp
import ch.smart.code.network.ResponseConfig

class CustomApp : SmartCodeApp() {

    companion object {
        const val API_DOMAIN = "api_domain"
    }

    override fun onCreate() {
        DEBUG = BuildConfig.DEBUG
        super.onCreate()
    }

    override fun getApiUrls(): MutableMap<String, String>? {
        //设置接口响应格式
        ResponseConfig.MSG_FIELD = "errmsg"
        ResponseConfig.CODE_FIELD = "code"
        ResponseConfig.DATA_FIELD = "content"
        ResponseConfig.SUCCESS_STATUS_CODE = "200"
        //提供API请求地址，支持多个
        return mutableMapOf<String, String>().apply {
            put(API_DOMAIN, BuildConfig.API_URL)
        }
    }
}