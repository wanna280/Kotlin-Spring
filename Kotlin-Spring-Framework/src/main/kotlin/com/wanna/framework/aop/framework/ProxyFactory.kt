package com.wanna.framework.aop.framework

/**
 * 这是一个ProxyFactory，可以通过它配置要进行代理的接口以及TargetSource等配置，最终通过getProxy去生成Aop代理
 */
open class ProxyFactory() : AdvisedSupport() {

    constructor(target: Any) : this() {
        // setTarget
        this.setTarget(target)
        // setInterfaces
        this.setInterfaces(*target::class.java.interfaces)
    }

    // 创建Aop动态代理的ProxyFactory
    private var aopProxyFactory: AopProxyFactory = DefaultAopProxyFactory()

    open fun setAopProxyFactory(aopProxyFactory: AopProxyFactory) {
        this.aopProxyFactory = aopProxyFactory
    }

    open fun getProxy(): Any {
        synchronized(this) {
            return createProxy().getProxy()
        }
    }

    open fun getProxy(classLoader: ClassLoader): Any {
        synchronized(this) {
            return createProxy().getProxy(classLoader)
        }
    }

    open fun createProxy(): AopProxy {
        return aopProxyFactory.createAopProxy(this)
    }
}