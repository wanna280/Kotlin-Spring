package com.wanna.framework.aop.support

import com.wanna.framework.aop.PointcutAdvisor
import com.wanna.framework.core.Ordered

/**
 * 基于Pointcut的Advisor的抽象实现
 *
 * @author jianchao.jia
 */
abstract class AbstractPointcutAdvisor : PointcutAdvisor, Ordered, java.io.Serializable {

    private var order: Int? = null

    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * 获取order
     */
    override fun getOrder(): Int {
        if (this.order != null) {
            return this.order!!
        }
        val advice = getAdvice()
        if (advice is Ordered) {
            return advice.getOrder()
        }
        return Ordered.ORDER_LOWEST
    }
}