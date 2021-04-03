package ch.smart.code.network

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

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request = httpHandler?.onHttpRequestBefore(chain, originalRequest) ?: originalRequest
        //打印请求信息
        if (openLog) {
            if (printer == null) {
                printer = FormatPrinter()
            }
            val printRequest = request.newBuilder().build()
            val rcType = printRequest.body()?.contentType()
            if (rcType != null && RequestUtils.isParseable(rcType)) {
                printer?.printJsonRequest(printRequest, RequestUtils.parseParams(printRequest))
            } else {
                printer?.printFileRequest(printRequest)
            }
        }

        //响应请求
        val startTime = System.nanoTime()
        val originalResponse = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Timber.e(e)
            throw e
        }
        val stopTime = System.nanoTime()

        //将请求结果转换成String
        val printResponse = originalResponse.newBuilder().build()
        val rcType = printResponse.body()?.contentType()
        val isParseable = rcType != null && RequestUtils.isParseable(rcType)
        val bodyString = if (isParseable) parseResult(printResponse) else null

        //打印响应
        if (openLog) {
            val time = TimeUnit.NANOSECONDS.toMillis(stopTime - startTime)
            val segmentList = request.url().encodedPathSegments()
            val header = printResponse.headers().toString()
            val code = printResponse.code()
            val isSuccessful = printResponse.isSuccessful
            val message = printResponse.message()
            val url = printResponse.request().url().toString()
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

    /**
     * 解析响应结果
     * @return 解析后的响应结果
     */
    private fun parseResult(printResponse: Response): String? {
        return try {
            val source = printResponse.body()?.source() ?: return null
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()
            buffer.readUtf8()
//            //读取服务器返回的结果
//            val responseBody = printResponse.body()
//            val buffer = responseBody?.source()?.buffer?.clone() ?: return null
//            //获取content的压缩类型
//            val encoding = printResponse.headers()["Content-Encoding"]
//            //解析response content
//            var charset = Charset.forName("UTF-8")
//            val contentType = responseBody.contentType()
//            if (contentType != null) {
//                charset = contentType.charset(charset)
//            }
//            when {
//                encoding.equals("gzip", ignoreCase = true) -> {
//                    //content 使用 gzip 压缩
//                    RequestUtils.decompressForGzip(
//                        buffer.readByteArray(),
//                        RequestUtils.convertCharset(charset)
//                    )
//                }
//                encoding.equals("zlib", ignoreCase = true) -> {
//                    //content 使用 zlib 压缩
//                    RequestUtils.decompressToStringForZlib(
//                        buffer.readByteArray(),
//                        RequestUtils.convertCharset(charset)
//                    )
//                }
//                else -> buffer.readString(charset)
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            "{\"error\": \"" + e.message + "\"}"
        }
    }
}