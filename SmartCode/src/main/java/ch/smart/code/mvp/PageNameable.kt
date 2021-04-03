package ch.smart.code.mvp

/**
 * 类描述：获取页面名称
 */
interface PageNameable {

    /**
     * 获取当前页面名称，一般用于数据统计，所以页面名称使用中文表达，便于产品运营人员查看
     */
    fun getPageName(): String

    /**
     * 获取页面路径，例如：首页-内容-音频
     */
    fun getPagePath(): String
}
