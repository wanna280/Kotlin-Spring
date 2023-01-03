package com.wanna.framework.core

import com.wanna.framework.util.ClassUtils

/**
 * 支持去进行嵌套的运行时异常，提供了原生的RuntimeException没有的关于cause的相关的功能的支持
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 * @param message 异常的信息
 * @param cause 造成当前异常的原因
 */
abstract class NestedRuntimeException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    /**
     * 提供一个只提供message的获取的构造器，对于cause我们直接填充null
     *
     * @param message message
     */
    constructor(message: String?) : this(message, null)

    /**
     * 获取当前这个异常的最顶层的Cause
     *
     * @return 当前异常的最顶层cause，如果不存在有顶层的cause的话，return null
     */
    fun getRootCause(): Throwable? = NestedExceptionUtils.getRootCause(this)

    /**
     * 获取产生当前异常的最根本的原因(要么是rootCause，要么是自身)
     *
     * @return  如果存在有rootCause，那么return rootCause；如果不存在的话，那么return null
     */
    fun getMostSpecificCause(): Throwable = NestedExceptionUtils.getMostSpecificCause(this) ?: this

    /**
     * 判断当前的异常(以及所有的cause)当中是否包含了给定的异常类型的异常？
     *
     * @param exType 异常类型
     * @return 如果内部确实包含了给定的exType，那么return true；否则return false
     */
    open operator fun contains(exType: Class<*>?): Boolean {
        exType ?: return false
        if (ClassUtils.isAssignFrom(exType, this::class.java)) {
            return true
        }
        var cause = this.cause
        if (cause === this) {
            return false
        }

        // 如果cause也是NestedRuntimeException，那么直接使用它的contains方法去进行递归检测
        return if (cause is NestedRuntimeException) {
            cause.contains(exType)
        } else {

            // 如果它不是一个NestedRuntimeException的话，那么我们遍历它的所有cause去进行判断
            while (cause != null) {
                if (ClassUtils.isAssignFrom(exType, cause::class.java)) {
                    return true
                }
                if (cause.cause === cause) {
                    break
                }
                cause = cause.cause
            }
            false
        }
    }

    /**
     * 当前异常的Message，我们在这里去进行重写，支持去获取内部的异常的message
     */
    override val message: String?
        get() = NestedExceptionUtils.buildMessage(super.message, cause)
}