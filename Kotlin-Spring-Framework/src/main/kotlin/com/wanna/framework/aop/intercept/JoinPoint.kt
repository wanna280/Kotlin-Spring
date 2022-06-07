package com.wanna.framework.aop.intercept

/**
 * 标识这是一个JoinPoint(连接点)
 */
interface JoinPoint {
    fun proceed(): Any?
    fun getThis(): Any?
}