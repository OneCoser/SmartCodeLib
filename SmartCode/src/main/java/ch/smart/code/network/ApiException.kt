package ch.smart.code.network

/**
 * 类描述：网络请求业务错误
 */
class ApiException(val code: String?, val msg: String?) : RuntimeException("code: $code  msg: $msg")
