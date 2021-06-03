package ch.smart.code.network

import ch.smart.code.SmartCodeApp
import io.reactivex.schedulers.Schedulers
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

/**
 * 描述: 网络请求构建器基类
 */
abstract class BaseApi {

    companion object{

    }

    fun <T> getApi(
        serviceClass: Class<T>,
        baseUrl: String = "https://api.github.com",
        responseRule: IResponseRule = CommonResponseRule()
    ): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(responseRule = responseRule))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build().create(serviceClass)
    }

    /**
     * 实现重写父类的setHttpClientBuilder方法，
     * 在这里可以添加拦截器，可以对 OkHttpClient.Builder 做任意操作
     */
    abstract fun setHttpClientBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder

    /**
     * 提供IHttpHandler
     */
    abstract fun provideHttpHandler(): IHttpHandler?

    /**
     * 提供不同Demain名称的Url
     */
    abstract fun provideDomainAndUrl(): Map<String, String>?

    /**
     * 配置http
     */
    private val okHttpClient: OkHttpClient by lazy {
        var builder = RetrofitUrlManager.getInstance().with(OkHttpClient.Builder())
        builder.addInterceptor(BasicInterceptor(provideHttpHandler(), SmartCodeApp.DEBUG))
        builder = setHttpClientBuilder(builder)
        val urls = provideDomainAndUrl()
        if (!urls.isNullOrEmpty()) {
            urls.keys.forEach { domain ->
                RetrofitUrlManager.getInstance().putDomain(domain, urls[domain])
            }
        }
        builder.build()
    }
}



