package com.wanna.framework.aop.framework

/**
 * 这是一个完成AopProxy的Factory工厂
 */
fun interface AopProxyFactory {
    fun createAopProxy(config: AdvisedSupport): AopProxy
}