package com.wanna.framework.aop

/**
 * 标识这是一个支持获取PointCut的Advisor，可以使用PointCut对方法和类去进行匹配，判断是否要生成代理；
 * 一个Pointcut会对应一个ClassFilter去对类去进行匹配，对应一个MethodMatcher去对方法去进行匹配
 *
 * @see Pointcut
 * @see Advisor
 * @see MethodMatcher
 * @see ClassFilter
 */
interface PointcutAdvisor : Advisor {
    /**
     * 获取Pointcut
     */
    fun getPointcut(): Pointcut
}