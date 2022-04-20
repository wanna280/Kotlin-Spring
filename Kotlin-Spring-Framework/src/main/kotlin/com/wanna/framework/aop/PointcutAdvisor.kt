package com.wanna.framework.aop

/**
 * 标识这是一个支持获取PointCut的Advisor，可以使用PointCut对方法和类去进行匹配，判断是否要生成代理
 */
interface PointcutAdvisor : Advisor {
    /**
     * 获取Pointcut
     */
    fun getPointcut(): Pointcut
}