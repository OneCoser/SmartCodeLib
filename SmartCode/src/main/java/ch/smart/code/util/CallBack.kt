package ch.smart.code.util

import timber.log.Timber

interface CallBack<T> {
    fun logError(): Boolean {
        return false
    }

    fun action(result: T?)
    fun actionReturn(result: T?): Any {
        action(result)
        return true
    }

    fun error(error: Throwable?) {
        if (logError()) {
            Timber.e(error)
        }
    }
}