package ch.smart.code.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

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