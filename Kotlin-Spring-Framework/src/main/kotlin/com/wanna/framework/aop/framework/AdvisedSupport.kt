package com.wanna.framework.aop.framework

import com.wanna.framework.aop.Advice
import com.wanna.framework.aop.Advisor
import com.wanna.framework.aop.TargetSource
import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.aop.intercept.MethodInvocation
import com.wanna.framework.aop.support.DefaultPointcutAdvisor
import com.wanna.framework.aop.target.EmptyTargetSource
import com.wanna.framework.aop.target.SingletonTargetSource
import java.lang.reflect.Method

/**
 * 这是一个对于Advised提供支持的类，也就是Advised的默认实现的通用模板类
 */
open class AdvisedSupport : ProxyConfig(), Advised {

    companion object {
        @JvmField
        val EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE
    }

    // 这是一个构建SpringAOP的拦截器链的工厂
    private var advisorChainFactory = DefaultAdvisorChainFactory()

    // targetSource
    private var targetSource: TargetSource = EMPTY_TARGET_SOURCE

    // 接口列表
    private val interfaces: MutableList<Class<*>> = ArrayList()

    // Advisor列表
    private val advisors: MutableList<Advisor> = ArrayList()

    open fun setTargetSource(targetSource: TargetSource?) {
        this.targetSource = targetSource ?: EMPTY_TARGET_SOURCE
    }

    open fun setInterfaces(vararg interfaces: Class<*>) {
        interfaces.forEach { this.interfaces += it }
    }

    open fun addInterface(itf: Class<*>) {
        this.interfaces += itf
    }

    open fun getInterfaces(): List<Class<*>> {
        return this.interfaces
    }

    /**
     * 设置target对象，使用TargetSource去进行封装一层
     *
     * @param target target对象
     */
    open fun setTarget(target: Any) {
        this.targetSource = SingletonTargetSource(target)
    }

    /**
     * 获取Interceptor和运行时的动态方法匹配的拦截器列表，也就是执行SpringAOP的拦截器列表
     */
    open fun getInterceptorsAndDynamicInterceptionAdvice(method: Method, targetClass: Class<*>?): List<Any> {
        return advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(this, method, targetClass)
    }

    /**
     * 获取TargetSource
     */
    open fun getTargetSource(): TargetSource {
        return this.targetSource
    }

    open fun getTargetClass(): Class<*>? {
        return this.targetSource.getTargetClass()
    }

    override fun addAdvisor(pos: Int, advisor: Advisor) {
        this.advisors[pos] = advisor
    }

    override fun addAdvisor(advisor: Advisor) {
        this.advisors += advisor
    }

    override fun addAdvice(advice: Advice) {
        addAdvice(advisors.size, advice)
    }

    override fun addAdvice(pos: Int, advice: Advice) {
        if (pos >= advisors.size) {
            this.advisors += DefaultPointcutAdvisor(advice)
        } else {
            this.advisors[pos] = DefaultPointcutAdvisor(advice)
        }
    }

    override fun getAdvisors(): Array<Advisor> {
        return this.advisors.toTypedArray()
    }
}

