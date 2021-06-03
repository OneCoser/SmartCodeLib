package ch.smart.code.bean

/**
 * 类描述：水印信息
 */
data class WatermarkBean(
    val text: String? = null,
    val size: Float = DEFAULT_SIZE,
    val autoLine: Boolean = true,
    val color: String? = DEFAULT_COLOR,
    val bgColor: String? = null,
    val gravity: String = GRAVITY_TOP,
    val marginLeft: Float = DEFAULT_SIZE,
    val marginRight: Float = DEFAULT_SIZE,
    val marginTop: Float = DEFAULT_SIZE,
    val marginBottom: Float = DEFAULT_SIZE
) {
    companion object {
        const val DEFAULT_SIZE = 0.02f
        const val DEFAULT_COLOR = "#000000"

        const val GRAVITY_TOP = "top"
        const val GRAVITY_CENTER = "center"
        const val GRAVITY_BOTTOM = "bottom"
    }
}