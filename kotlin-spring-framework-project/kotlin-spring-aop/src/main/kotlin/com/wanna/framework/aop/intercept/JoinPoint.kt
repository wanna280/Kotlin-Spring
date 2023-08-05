package com.wanna.framework.aop.intercept

import com.wanna.framework.lang.Nullable

/**
 * 标识这是一个JoinPoint(连接点)
 */
interface JoinPoint {

    /**
     * proceed, 将AOP的链条放行到下一环
     *
     * @return 执行目标Joinpoint方法得到的最终结果
     */
    @Nullable
    fun proceed(): Any?

    /**
     * 获取到Joinpoint所处的当前对象
     *
     * @return this objest
     */
    @Nullable
    fun getThis(): Any?
}