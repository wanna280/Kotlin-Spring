package com.wanna.framework.aop

/**
 * 标识这是一个Advisor
 */
interface Advisor {
    /**
     * 获取Advice
     */
    fun getAdvice(): Advice
}