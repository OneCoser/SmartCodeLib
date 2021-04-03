package ch.smart.code.network

import org.json.JSONObject

/**
 * 类描述：接口响应数据基类解析规则
 */
interface IResponseRule {

    /**
     * 接口是否响应错误，错误就返回ApiException，成功返回null
     */
    fun isError(responseData: JSONObject): ApiException?

    /**
     * 接口请求成功时获取数据的字段名
     */
    fun getDataField(): String
}