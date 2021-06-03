package ch.smart.code.network

import ch.smart.code.util.bodyToString
import ch.smart.code.util.isNotNullOrBlank
import com.blankj.utilcode.util.EncryptUtils
import com.tencent.mmkv.MMKV
import okhttp3.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 类描述：网络请求拦截器-做一些框架基础逻辑
 */
class BasicInterceptor(
    private val httpHandler: IHttpHandler?,
    private val openLog: Boolean
) : Interceptor {

    private var printer: FormatPrinter? = null
    private val apiCache by lazy {
        MMKV.mmkvWithID("basic_api_cache")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request = httpHandler?.onHttpRequestBefore(chain, originalRequest) ?: originalRequest
        val copyRequest = request.newBuilder().build()
        //打印请求信息
        if (openLog) {
            if (printer == null) {
                printer = FormatPrinter()
            }
            val rcType = copyRequest.body()?.contentType()
            if (rcType != null && RequestUtils.isParseable(rcType)) {
                printer?.printJsonRequest(copyRequest, RequestUtils.parseParams(copyRequest))
            } else {
                printer?.printFileRequest(copyRequest)
            }
        }
        //缓存操作
        val cacheType = httpHandler?.getCacheType(copyRequest) ?: IHttpHandler.CACHE_TYPE_NONE
        val cacheKey = if (
            cacheType == IHttpHandler.CACHE_TYPE_FIX || cacheType == IHttpHandler.CACHE_TYPE_ERROR
        ) {
            try {
                val originalKey =
                    String.format(
                        "%s?%s",
                        copyRequest.url().toString(),
                        copyRequest.bodyToString()
                    )
                val md5Key = EncryptUtils.encryptMD5ToString(originalKey)
                if (md5Key.isNullOrBlank()) originalKey else md5Key
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
        } else {
            null
        }

        //响应请求
        val startTime = System.nanoTime()
        val cacheResponse = if (cacheKey.isNotNullOrBlank()) {
            try {
                httpHandler?.createCacheResponse(copyRequest, apiCache?.decodeString(cacheKey))
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
        } else null
        var canSaveCache = false
        val originalResponse =
            if (cacheType == IHttpHandler.CACHE_TYPE_FIX && cacheResponse != null) {
                cacheResponse
            } else {
                val apiResponse = try {
                    chain.proceed(request)
                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }
                when {
                    apiResponse?.isSuccessful == true -> {
                        canSaveCache = true
                        apiResponse
                    }
                    cacheType == IHttpHandler.CACHE_TYPE_ERROR && cacheResponse != null -> cacheResponse
                    else -> throw Exception("网络请求失败!")
                }
            }
        val stopTime = System.nanoTime()

        //将请求结果转换成String
        val copyResponse = originalResponse.newBuilder().build()
        val rcType = copyResponse.body()?.contentType()
        val isParseable = rcType != null && RequestUtils.isParseable(rcType)
        val bodyString =
            if (isParseable) copyResponse.bodyToString(defaultStr = "{\"error\": \"数据解析错误\"}") else null

        //判断是否需要缓存数据
        if (canSaveCache && cacheKey.isNotNullOrBlank() && bodyString.isNotNullOrBlank()
            && httpHandler?.checkSaveCache(copyRequest, bodyString) == true
        ) {
            apiCache?.encode(cacheKey, bodyString)
        }

        //打印响应
        if (openLog) {
            val time = TimeUnit.NANOSECONDS.toMillis(stopTime - startTime)
            val segmentList = request.url().encodedPathSegments()
            val header = copyResponse.headers().toString()
            val code = copyResponse.code()
            val isSuccessful = copyResponse.isSuccessful
            val message = copyResponse.message()
            val url = copyResponse.request().url().toString()
            if (isParseable) {
                printer?.printJsonResponse(
                    time,
                    isSuccessful,
                    code,
                    header,
                    rcType,
                    bodyString,
                    segmentList,
                    message,
                    url
                )
            } else {
                printer?.printFileResponse(
                    time,
                    isSuccessful,
                    code,
                    header,
                    segmentList,
                    message,
                    url
                )
            }
        }

        return httpHandler?.onHttpResultResponse(bodyString, chain, originalResponse)
            ?: originalResponse
    }
}