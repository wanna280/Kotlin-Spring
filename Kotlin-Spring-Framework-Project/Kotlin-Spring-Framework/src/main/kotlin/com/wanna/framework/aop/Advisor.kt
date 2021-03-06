package com.wanna.framework.aop

/**
 * 标识这是一个Spring当中的Advisor(增强器)，它可以获取到用来对Bean去进行增强的Advice；
 * 它的最常见的实现是PointcutAdvisor，支持使用Pointcut去对方法和类去进行匹配，从而判断是否需要对目标Bean去生成代理
 *
 * @see Advice
 * @see PointcutAdvisor
 * @see Pointcut
 */
interface Advisor {
    /**
     * 获取Advice
     */
    fun getAdvice(): Advice
}