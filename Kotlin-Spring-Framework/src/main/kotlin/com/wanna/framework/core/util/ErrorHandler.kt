package com.wanna.framework.core.util

/**
 * 这是一个处理一场的Handler
 */
interface ErrorHandler {
    fun handleError(ex: Throwable)
}