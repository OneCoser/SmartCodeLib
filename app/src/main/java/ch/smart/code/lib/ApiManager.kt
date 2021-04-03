package ch.smart.code.lib

import ch.smart.code.network.BaseApi
import ch.smart.code.network.IHttpHandler
import ch.smart.code.network.OkHttpFactory
import okhttp3.OkHttpClient
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
        return AppHttpHandler()
    }

    override fun provideDomainAndUrl(): Map<String, String>? {
        return urls
    }
}