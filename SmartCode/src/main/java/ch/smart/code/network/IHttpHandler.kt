package ch.smart.code.network

import ch.smart.code.util.isNotNullOrBlank
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * 项目名称：Consignor
 * 类描述：
 * 创建人：chenhao
 * 创建时间：3/12/21 8:00 PM
 * 修改人：chenhao
 * 修改时间：3/12/21 8:00 PM
 * 修改备注：
 * @version
 */
interface IHttpHandler {

    companion object {
        const val CACHE_TYPE_NONE = 0//不需要缓存
        const val CACHE_TYPE_FIX = 1//存在缓存就使用
        const val CACHE_TYPE_ERROR = 2//请求失败了才使用缓存
    }

    /**
     * 获取请求缓存类型
     */
    fun getCacheType(request: Request): Int {
        return CACHE_TYPE_NONE
    }

    /**
     * 创建缓存响应数据
     */
    fun createCacheResponse(request: Request, cacheStr: String?): Response? {
        return if (cacheStr.isNotNullOrBlank()) {
            Response.Builder()
                .code(200)
                .message("获取缓存")
                .request(request)
                .protocol(Protocol.HTTP_1_0)
                .body(cacheStr.toResponseBody("application/json; charset=UTF-8".toMediaTypeOrNull()))
                .build()
        } else null
    }

    /**
     * 响应数据是否需要缓存
     */
    fun checkSaveCache(request: Request, responseStr: String): Boolean {
        return false
    }

    /**
     * 这里可以在请求服务器之前拿到 [Request], 做一些操作比如给 [Request] 统一添加 token 或者 header 以及参数加密等操作
     *
     * @param chain   [okhttp3.Interceptor.Chain]
     * @param request [Request]
     * @return [Request]
     */
    fun onHttpRequestBefore(chain: Interceptor.Chain, request: Request): Request

    /**
     * 这里可以先一步拿到 Http 请求的结果, 可以先解析成 Json, 再做一些操作
     *
     * @param httpResult 服务器返回的结果 (已被自动转换为字符串)
     * @param chain      [okhttp3.Interceptor.Chain]
     * @param response   [Response]
     * @return [Response]
     */
    fun onHttpResultResponse(
        httpResult: String?,
        chain: Interceptor.Chain,
        response: Response
    ): Response
}