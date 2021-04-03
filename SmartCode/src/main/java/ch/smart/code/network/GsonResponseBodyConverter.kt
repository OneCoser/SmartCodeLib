package ch.smart.code.network

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Converter
import java.io.IOException
import java.io.StringReader
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class GsonResponseBodyConverter<T>(
    private val gson: Gson,
    private val responseRule: IResponseRule,
    private val adapter: TypeAdapter<T>,
    private val isArray: Boolean,
    private val type: Type
) : Converter<ResponseBody, T> {

    @Suppress("TooGenericExceptionThrown")
    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {
        try {
            val responseStr = value.string()
            if (isResponseType()) {
                adapter.read(gson.newJsonReader(StringReader(responseStr)))
            }
            val json = JSONObject(responseStr)
            val error = responseRule.isError(json)
            if (error != null) {
                throw error
            }
            val data = json.optString(responseRule.getDataField())
            val isNull = data.isNullOrBlank() || data.isNullOrEmpty() || data == "null"
            return when {
                isArray && isNull -> adapter.read(gson.newJsonReader(StringReader("[]")))
                isNull -> when (type) {
                    String::class.java -> "" as T
                    Boolean::class.java -> java.lang.Boolean.FALSE as T
                    Int::class.java -> 0 as T
                    Long::class.java -> 0L as T
                    else -> adapter.read(gson.newJsonReader(StringReader("{}")))
                }
                type == String::class.java -> data as T
                else -> adapter.read(gson.newJsonReader(StringReader(data)))
            }
        } catch (e: JSONException) {
            throw ApiException(code = "500", msg = String.format("数据解析错误:%s", e.message))
        } finally {
            value.close()
        }
    }

    private fun isResponseType(): Boolean {
        val checkType = if (type is ParameterizedType) {
            type.rawType
        } else {
            type
        }
        GsonConverterFactory.RESPONSE_CLASS.forEach {
            if (checkType == it) {
                return true
            }
        }
        return false
    }
}
