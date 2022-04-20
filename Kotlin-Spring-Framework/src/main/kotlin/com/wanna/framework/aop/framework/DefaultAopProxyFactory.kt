package com.wanna.framework.aop.framework

/**
 * 这是一个默认的AopProxy工厂
 */
class DefaultAopProxyFactory : AopProxyFactory {
    override fun createAopProxy(config: AdvisedSupport): AopProxy {
        val targetClass = config.getTargetClass() ?: throw IllegalStateException("无法从TargetSource当中获取的代理的targetClass")
        if (targetClass.interfaces.isNotEmpty()) {
            return JdkDynamicAopProxy(config)
        }
        return CglibAopProxy(config)
    }
}