package com.wanna.framework.aop

import java.lang.reflect.Method

/**
 * 方法匹配器, 支持对一个类上的的具体的方法去进行匹配
 *
 * @see TrueMethodMatcher
 * @see ClassFilter
 * @see PointcutAdvisor
 */
interface MethodMatcher {

    companion object {

        /**
         * 对于方法匹配直接返回True的实例
         */
        @JvmField
        val TRUE = TrueMethodMatcher.INSTANCE
    }

    /**
     * 在Spring BeanFactory启动时去匹配一个方法
     *
     * @param method 要进行匹配的方法
     * @param targetClass 要进行匹配的目标类
     * @return 是否匹配成功? 如果return true, 将会对该对象去生成代理
     */
    fun matches(method: Method, targetClass: Class<*>): Boolean

    /**
     * 是否需要在运行时去进行匹配？运行时可以将执行该方法的参数也传递给你去进行匹配
     *
     * @return 如果需要在运行时去进行匹配, return true; 否则return false
     */
    fun isRuntime(): Boolean

    /**
     * 在运行时匹配一个方法，会将运行时的参数列表一起进行匹配
     *
     * @param method 要进行匹配的方法
     * @param targetClass 要进行匹配的目标类
     * @param args 方法的参数列表
     * @return 是否匹配成功? 如果return true, 将会对该对象去生成代理
     */
    fun matches(method: Method, targetClass: Class<*>, vararg args: Any?): Boolean
}