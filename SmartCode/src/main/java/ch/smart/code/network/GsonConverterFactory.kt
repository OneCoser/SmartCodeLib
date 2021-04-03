package ch.smart.code.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class GsonConverterFactory private constructor(
    private val gson: Gson,
    private val responseRule: IResponseRule
) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val typeToken = TypeToken.get(type)
        return GsonResponseBodyConverter(
            gson,
            responseRule,
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
        fun create(
            gson: Gson = GsonBuilder().setLenient().create(),
            responseRule: IResponseRule = CommonResponseRule()
        ): GsonConverterFactory {
            return GsonConverterFactory(gson = gson, responseRule = responseRule)
        }

        val RESPONSE_CLASS: ArrayList<Class<*>> by lazy { arrayListOf<Class<*>>() }
    }
}
