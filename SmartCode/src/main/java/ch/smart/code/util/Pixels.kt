package ch.smart.code.util

import android.util.TypedValue
import com.blankj.utilcode.util.Utils

@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "dp",
        "ch.smart.code.util.dp"
    )
)
fun Int.dp2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}

@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "sp",
        "ch.smart.code.util.sp"
    )
)
fun Int.sp2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}

@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "pt",
        "ch.smart.code.util.pt"
    )
)
fun Int.pt2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PT,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}

@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "in",
        "ch.smart.code.util.in"
    )
)
fun Int.in2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_IN,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}

@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "mm",
        "ch.smart.code.util.mm"
    )
)
fun Int.mm2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_MM,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}


@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "dp",
        "ch.smart.code.util.dp"
    )
)
fun Float.dp2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}

@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "sp",
        "ch.smart.code.util.sp"
    )
)
fun Float.sp2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}

@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "pt",
        "ch.smart.code.util.pt"
    )
)
fun Float.pt2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PT,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}

@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "in",
        "ch.smart.code.util.in"
    )
)
fun Float.in2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_IN,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}

@Deprecated(
    "扩展方法简化成扩展属性，使代码更精简和清晰",
    ReplaceWith(
        "mm",
        "ch.smart.code.util.mm"
    )
)
fun Float.mm2px(): Int {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_MM,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
}

val Int.dp: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()


val Int.sp: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()


val Int.pt: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PT,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()


val Int.`in`: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_IN,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()


val Int.mm: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_MM,
        this.toFloat(),
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()


val Float.dp: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()


val Float.sp: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()

val Float.pt: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PT,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()


val Float.`in`: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_IN,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()


val Float.mm: Int
    get() = (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_MM,
        this,
        Utils.getApp().resources.displayMetrics
    ) + 0.5f).toInt()
