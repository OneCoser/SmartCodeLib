package ch.smart.code.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class GsonConverterFactory private constructor(private val gson: Gson) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val typeToken = TypeToken.get(type)
        return GsonResponseBodyConverter(
            gson,
            gson.getAdapter(typeToken),
            typeToken.rawType.name == "java.util.List",
            type
        )
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        return GsonRequestBodyConverter(gson, gson.getAdapter(TypeToken.get(type)))
    }

    companion object {
        @JvmOverloads
        fun create(gson: Gson? = Gson()): GsonConverterFactory {
            if (gson == null) {
                throw NullPointerException("gson == null")
            }
            return GsonConverterFactory(gson)
        }
    }
}