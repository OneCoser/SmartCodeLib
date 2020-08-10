package ch.smart.code.util

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.service.SerializationService
import ch.smart.code.util.json
import java.lang.reflect.Type

@Route(path = ARouterJsonService.PATH)
class ARouterJsonService : SerializationService {

    companion object {
        const val PATH = "smart.code.arouter.json"
    }

    override fun init(context: Context?) {
    }

    override fun object2Json(instance: Any?): String {
        return json.toJson(instance)
    }

    override fun <T : Any?> parseObject(input: String?, clazz: Type?): T {
        return json.fromJson(input, clazz)
    }

    override fun <T : Any?> json2Object(input: String?, clazz: Class<T>?): T {
        return json.fromJson(input, clazz)
    }
}
