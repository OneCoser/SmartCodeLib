package ch.smart.code.network

import java.lang.RuntimeException

class ApiException(val code: String?, val msg: String?) : RuntimeException("code: $code  msg: $msg")