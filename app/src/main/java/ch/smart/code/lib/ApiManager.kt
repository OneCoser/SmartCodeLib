package ch.smart.code.lib

import ch.smart.code.network.BaseApi
import ch.smart.code.network.IHttpHandler
import ch.smart.code.network.OkHttpFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * 类描述：Api访问类
 */
object ApiManager : BaseApi() {

    const val API_DOMAIN = "api_domain"

    private val urls by lazy {
        mutableMapOf<String, String>().apply {
            this[API_DOMAIN] = BuildConfig.API_URL
        }
    }

    override fun setHttpClientBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        builder.connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        return OkHttpFactory.copyCommonHttpClient(builder)
    }

    override fun provideHttpHandler(): IHttpHandler? {
        return object : IHttpHandler {

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
    }

    override fun provideDomainAndUrl(): Map<String, String>? {
        return urls
    }
}