package ch.smart.code.util

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils

/**
 * 类描述：APP运行环境工具类
 */

internal const val dev = "dev"
internal const val test = "_test"
internal const val uat = "uat"
internal const val uatIP = "uatIP"
internal const val production = "production"
internal const val alpha = "alpha"

const val API_ENVIRONMENT_FLAVOR_CONFIG = "API_ENVIRONMENT_FLAVOR_CONFIG"

enum class AppEnvironment(val flavor: String) {
    DEV(dev),
    TEST(test),
    UAT(uat),
    UATIP(uatIP),
    ALPHA(alpha),
    PRODUCTION(production),
}

var appEnvironment = AppEnvironment.PRODUCTION

fun createAppEnvironment(flavor: String): AppEnvironment {
    return when (flavor) {
        dev -> AppEnvironment.DEV
        test -> AppEnvironment.TEST
        uat -> AppEnvironment.UAT
        alpha -> AppEnvironment.ALPHA
        else -> AppEnvironment.PRODUCTION
    }
}

@Suppress("ComplexCondition")
private fun decideEnvironment(environment: AppEnvironment): Boolean {
    return environment != AppEnvironment.PRODUCTION
}

/**
 * 初始化api环境
 */
fun initAppEnvironment(environment: AppEnvironment) {
    appEnvironment = if (decideEnvironment(environment)) {
        val localFlavor = SPUtils.getInstance().getString(API_ENVIRONMENT_FLAVOR_CONFIG, "")
        if (localFlavor.isNotNullOrEmpty()) {
            createAppEnvironment(localFlavor)
        } else {
            environment
        }
    } else {
        environment
    }
    LogUtils.i("appEnvironment:$appEnvironment")
}

/**
 * 切换Api环境
 */
fun changeAppEnvironment(environment: AppEnvironment) {
    if (decideEnvironment(environment)) {
        SPUtils.getInstance()
            .put(API_ENVIRONMENT_FLAVOR_CONFIG, environment.flavor)
        appEnvironment = environment
    }
}

/**
 * 环境描述
 */
fun getAppEnvironmentDes(): String {
    return when (appEnvironment) {
        AppEnvironment.DEV -> "开发"
        AppEnvironment.TEST -> "测试"
        AppEnvironment.UAT -> "验证"
        AppEnvironment.UATIP -> "验证IP"
        AppEnvironment.ALPHA -> "预发布"
        else -> "生产"
    }
}

/**
 * 是否为开发环境
 */
fun isDev(): Boolean {
    return appEnvironment == AppEnvironment.DEV
}

/**
 * 是否为测试环境
 */
fun isTest(): Boolean {
    return appEnvironment == AppEnvironment.TEST
}

/**
 * 是否为验证环境
 */
fun isVerify(): Boolean {
    return appEnvironment == AppEnvironment.UAT || appEnvironment == AppEnvironment.UATIP
}

/**
 * 是否为预发布环境
 */
fun isPreview(): Boolean {
    return appEnvironment == AppEnvironment.ALPHA
}

/**
 * 是否为线上生产环境
 */
fun isProduction(): Boolean {
    return appEnvironment == AppEnvironment.PRODUCTION
}
