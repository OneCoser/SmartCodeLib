package ch.smart.code.network

import org.json.JSONObject

/**
 * 类描述：接口响应数据基类解析字段模版,适用于在线项目的接口
 */
class CommonResponseRule : IResponseRule {

    override fun isError(responseData: JSONObject): ApiException? {
        val code = responseData.optString("code")
        if (code != "200") {
            return ApiException(
                code = code,
                msg = responseData.optString("msg")
            )
        }
        return null
    }

    override fun getDataField(): String {
        return "data"
    }
}
