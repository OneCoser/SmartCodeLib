package ch.smart.code

import android.app.Application
import android.content.Context
import androidx.fragment.app.FragmentManager
import ch.smart.code.network.GsonConverterFactory
import ch.smart.code.util.SCPrinter
import ch.smart.code.util.SCThreadFactory
import com.google.gson.GsonBuilder
import com.jess.arms.base.delegate.AppLifecycles
import com.jess.arms.di.module.GlobalConfigModule
import com.jess.arms.http.log.RequestInterceptor
import com.jess.arms.integration.ConfigModule
import io.reactivex.schedulers.Schedulers
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit

class SCConfigModule : ConfigModule {
    
    override fun applyOptions(context: Context, builder: GlobalConfigModule.Builder) {
        if (BuildConfig.DEBUG) {
            builder.formatPrinter(SCPrinter())
        } else {
            builder.printHttpLogLevel(RequestInterceptor.Level.NONE)
        }.baseurl("https://www.youtube.com/")
                .okhttpConfiguration { _, it ->
                    it.connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                    // 让 Retrofit 同时支持多个 BaseUrl 以及动态改变 BaseUrl. 详细使用请方法查看 https://github.com/JessYanCoding/RetrofitUrlManager
                    RetrofitUrlManager.getInstance().with(it)
                }
                .retrofitConfiguration { _, builder1 ->
                    builder1.addConverterFactory(
                            GsonConverterFactory.create(
                                    GsonBuilder().setLenient().create()
                            )
                    )
                    builder1.addCallAdapterFactory(
                            RxJava2CallAdapterFactory.createWithScheduler(
                                    Schedulers.io()
                            )
                    )
                }.executorService(SCThreadFactory.createExecutorService())
    }
    
    override fun injectAppLifecycle(context: Context, lifecycles: MutableList<AppLifecycles>) {
    
    }
    
    override fun injectActivityLifecycle(context: Context, lifecycles: MutableList<Application.ActivityLifecycleCallbacks>) {
        lifecycles.add(SCActivityLifecycleCallback())
    }
    
    override fun injectFragmentLifecycle(context: Context, lifecycles: MutableList<FragmentManager.FragmentLifecycleCallbacks>) {
        lifecycles.add(SCFragmentLifecycleCallback())
    }
}
