package ch.smart.code.lib

import io.reactivex.Observable
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers(RetrofitUrlManager.DOMAIN_NAME_HEADER + CustomApp.API_DOMAIN)
    @POST("fetch_separation_position")
    fun loadList(@Body request: Map<String, @JvmSuppressWildcards Any>): Observable<List<String>>

}
