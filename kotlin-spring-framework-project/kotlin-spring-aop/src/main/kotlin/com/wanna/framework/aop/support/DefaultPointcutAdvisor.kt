package com.wanna.framework.aop.support

import com.wanna.framework.aop.Advice
import com.wanna.framework.aop.Pointcut

/**
 * 这是一个默认的PointcutAdvisor的实现, 如果给定了Pointcut, 那么将会采用给定的Pointcut去完成类/方法的匹配; 
 * 如果没有给定Pointcut, 将会采用默认的TRUE作为Pointcut, 不对方法进行匹配, Advice一律生效
 */
open class DefaultPointcutAdvisor(private val pointcut: Pointcut, private var advice: Advice?) :
    AbstractPointcutAdvisor() {
    constructor(advice: Advice?) : this(Pointcut.TRUE, advice)
    constructor() : this(null)

    /**
     * getAdvice, 如果没有设置就get, 那么直接抛异常出去
     */
    override fun getAdvice(): Advice {
        return this.advice!!
    }

    override fun getPointcut(): Pointcut {
        return this.pointcut
    }

    open fun setAdvice(advice: Advice) {
        this.advice = advice
    }
}