package com.wanna.framework.core

import com.wanna.framework.lang.Nullable

/**
 * 提供嵌套的异常的处理相关工具方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 * @see NestedRuntimeException
 */
object NestedExceptionUtils {

    /**
     * 构建一个提供了内部的Exception的message的方法
     *
     * @param message 外层异常的message
     * @param cause 内层异常(造成外层异常的原因)
     * @return 包含有当前异常和内层异常的原因的message
     */
    @Nullable
    @JvmStatic
    fun buildMessage(@Nullable message: String?, @Nullable cause: Throwable?): String? {
        cause ?: return message
        val builder = StringBuilder()
        if (message != null) {
            builder.append(message).append(" ;")
        }
        builder.append("nested exception is: ").append(cause.message)
        return builder.toString()
    }

    /**
     * 获取产生当前给定的异常的最根本的原因(要么是rootCause, 要么是自身)
     *
     * @param origin 想要去进行检查的异常
     * @return 如果origin存在有rootCause, 那么return rootCause; 如果origin不存在有rootCause的话, 那么return null
     */
    @Nullable
    @JvmStatic
    fun getMostSpecificCause(@Nullable origin: Throwable?): Throwable? = getRootCause(origin) ?: origin


    /**
     * 用于去获取一个异常的最顶层原因(cause)的异常.
     *
     * 因为对于每个异常来说, 都有可能会存在有cause, 而对于cause这个异常, 也会存在有cause,
     * 就这样无穷无尽形成链表, 对于这个方法来说, 我们需要获取到给定的异常的最顶层的cause异常.
     *
     * @param origin 最原始的异常
     * @return 返回最内层的异常, 如果不存在最内层异常的话, return null
     */
    @Nullable
    @JvmStatic
    fun getRootCause(@Nullable origin: Throwable?): Throwable? {
        origin ?: return null
        var rootCause: Throwable? = null
        var cause: Throwable? = origin.cause

        while (cause != null && cause != rootCause) {
            rootCause = cause
            cause = cause.cause
        }
        return rootCause
    }
}