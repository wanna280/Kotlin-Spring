package com.wanna.framework.aop.intercept

/**
 * 标识这是一个JoinPoint
 */
interface JoinPoint {
    fun proceed(): Any?

    fun getThis(): Any?
}