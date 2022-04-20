package com.wanna.framework.aop.framework

import com.wanna.framework.aop.Advisor
import com.wanna.framework.aop.TargetSource
import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.aop.intercept.MethodInvocation
import com.wanna.framework.aop.target.EmptyTargetSource
import com.wanna.framework.aop.target.SingletonTargetSource
import java.lang.reflect.Method

open class AdvisedSupport {

    companion object {
        @JvmField
        val EMPTY_TARGET_SOURCR = EmptyTargetSource.INSTANCE
    }

    // targetSource
    private var targetSource: TargetSource = EMPTY_TARGET_SOURCR

    // 接口列表
    private val interfaces: MutableList<Class<*>> = ArrayList()

    // Advisor列表
    private val advisors: MutableList<Advisor> = ArrayList()

    open fun setTargetSource(targetSource: TargetSource?) {
        this.targetSource = targetSource ?: EMPTY_TARGET_SOURCR
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
     */
    open fun setTarget(target: Any) {
        this.targetSource = SingletonTargetSource(target)
    }

    /**
     * 获取Interceptor和运行时的动态方法匹配的Advice
     */
    open fun getInterceptorsAndDynamicInterceptionAdvice(method: Method, targetClass: Class<*>?): List<Any> {
        val interceptors: ArrayList<MethodInterceptor> = ArrayList()
        interceptors.add(object : MethodInterceptor {
            override fun invoke(invocation: MethodInvocation): Any? {
                println("before")
                val returnVal = invocation.proceed()
                println("after")
                return returnVal
            }
        })
        return interceptors
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

}