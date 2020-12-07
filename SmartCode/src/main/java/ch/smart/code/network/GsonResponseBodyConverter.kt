package ch.smart.code.network

import ch.smart.code.SmartCodeApp
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Converter
import timber.log.Timber
import java.io.IOException
import java.io.StringReader
import java.lang.reflect.Type

internal class GsonResponseBodyConverter<T>(
    private val gson: Gson,
    private val adapter: TypeAdapter<T>,
    private val type: Type
) : Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {
        var responseStr: String? = null
        return try {
            responseStr = value.string()
            if (SmartCodeApp.DEBUG) {
                Timber.i("Http：%s", responseStr)
            }
            val json = JSONObject(responseStr)
            val code = json.optString(ResponseConfig.CODE_FIELD)
            if (code == ResponseConfig.SUCCESS_STATUS_CODE) {
                val isList = type == List::class.java
                getT(
                    when {
                        json.has(ResponseConfig.DATA_FIELD) -> json.optString(ResponseConfig.DATA_FIELD)
                        isList -> "[]"
                        else -> "{}"
                    }
                    , isList
                )
            } else {
                throw ApiException(code, json.optString(ResponseConfig.MSG_FIELD))
            }
        } catch (e: JSONException) {
            Timber.e("JSONException json:%s", responseStr)
            throw RuntimeException(e)
        } finally {
            value.close()
        }
    }

    @Throws(IOException::class)
    private fun getT(data: String?, isList: Boolean): T {
        val isNull = data.isNullOrBlank() || data.isNullOrEmpty() || data == "null"
        if (isList && isNull) {
            return adapter.read(gson.newJsonReader(StringReader("[]")))
        }
        if (isNull) {
            when (type) {
                String::class.java -> {
                    return "" as T
                }
                Boolean::class.java -> {
                    return java.lang.Boolean.FALSE as T
                }
                Int::class.java -> {
                    return 0 as T
                }
                Long::class.java -> {
                    return 0L as T
                }
                else -> {
                    return Any() as T
                }
            }
        }
        if (type == String::class.java) {
            return data as T
        }
        return adapter.read(gson.newJsonReader(StringReader(data)))
    }
}