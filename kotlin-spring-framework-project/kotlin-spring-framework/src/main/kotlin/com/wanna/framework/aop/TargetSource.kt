package com.wanna.framework.aop

/**
 * 这是对Target对象的来源进行提供的方式, 是对执行代理方法的target对象的来源的抽象,
 * 对于普通的代理来说, 一般使用单例的TargetSource即可
 * @see com.wanna.framework.aop.target.SingletonTargetSource
 *
 * 在执行代理方法时, 会自动从TargetSource当中获取Target对象
 * @see com.wanna.framework.aop.framework.JdkDynamicAopProxy.invoke
 * @see com.wanna.framework.aop.framework.CglibAopProxy
 *
 * 对于某些特殊需要的Bean来说, 可以自己去自定义TargetSource,
 * 可以通过自定义TargetSourceCreator的方式去进行实现, 并将其注册到AbstractAutoProxyCreator当中
 * @see com.wanna.framework.aop.creator.AbstractAutoProxyCreator.customTargetSourceCreators
 * @see com.wanna.framework.aop.creator.AbstractAutoProxyCreator.postProcessBeforeInitialization
 */
interface TargetSource {

    /**
     * 获取target对象的类型
     *
     * @return targetClass
     */
    fun getTargetClass(): Class<*>?

    fun isStatic(): Boolean

    /**
     * 获取target对象
     *
     * @return target
     */
    fun getTarget(): Any?

    /**
     * 释放目标对象
     *
     * @param target target
     */
    fun releaseTarget(target: Any?)
}