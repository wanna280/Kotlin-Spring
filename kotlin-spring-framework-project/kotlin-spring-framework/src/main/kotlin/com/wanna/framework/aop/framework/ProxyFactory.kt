package com.wanna.framework.aop.framework

/**
 * ## 1.ProxyFactory是什么？
 *
 * ProxyFactory是AdvisedSupport的子类, 可以通过它配置要进行代理的接口以及TargetSource等配置, 最终通过getProxy去生成Aop代理; 
 * 对于Spring当中去创建Aop代理, 这个类是SpringAop的一个入口类, 只需要去自定义ProxyFactory的TargetSource, 即可完成代理; 
 *
 * ## 2.ProxyFactory如何去创建代理
 *
 * 它会根据你是否有接口去进行匹配代理方式.如果有接口, 那么默认采用Jdk的动态代理; 如果没有接口, 那么会采用Cglib去完成代理; 
 * 你也可以通过设置proxyTargetClass=true去强制设置为Cglib动态代理
 *
 * @see AdvisedSupport
 * @see ProxyCreatorSupport
 * @see com.wanna.framework.aop.TargetSource
 * @see proxyTargetClass
 */
open class ProxyFactory() : ProxyCreatorSupport() {
    constructor(target: Any) : this() {
        // setTarget
        this.setTarget(target)
        // setInterfaces
        this.setInterfaces(*target::class.java.interfaces)
    }

    open fun getProxy(): Any {
        synchronized(this) {
            return createProxy().getProxy()
        }
    }

    open fun getProxy(classLoader: ClassLoader?): Any {
        synchronized(this) {
            return createProxy().getProxy(classLoader)
        }
    }

    open fun createProxy(): AopProxy {
        return aopProxyFactory.createAopProxy(this)
    }
}