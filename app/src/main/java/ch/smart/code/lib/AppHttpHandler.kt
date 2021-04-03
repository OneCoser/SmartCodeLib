package ch.smart.code.lib

import ch.smart.code.network.IHttpHandler
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * 类描述：网络请求添加请求头
 */
class AppHttpHandler : IHttpHandler {

    override fun onHttpRequestBefore(chain: Interceptor.Chain, request: Request): Request {
        //添加公共请求头信息
        val builder = request.newBuilder()
        builder.addHeader("token", "123456")
        return builder.build()
    }

    override fun onHttpResultResponse(
        httpResult: String?,
        chain: Interceptor.Chain,
        response: Response
    ): Response {
        return response
    }
}