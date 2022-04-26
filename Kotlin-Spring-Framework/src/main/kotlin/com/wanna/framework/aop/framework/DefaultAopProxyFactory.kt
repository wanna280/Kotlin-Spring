package com.wanna.framework.aop.framework

/**
 * 这是一个默认的AopProxy工厂，主要根据情况去决定采用何种方式去完成动态代理
 */
class DefaultAopProxyFactory : AopProxyFactory {
    override fun createAopProxy(config: AdvisedSupport): AopProxy {
        val targetClass = config.getTargetClass() ?: throw IllegalStateException("无法从TargetSource当中获取的代理的targetClass")

        if (targetClass.interfaces.isNotEmpty()) {
            if (config.proxyTargetClass) {
                return CglibAopProxy(config)
            }
            return JdkDynamicAopProxy(config)
        }
        return CglibAopProxy(config)
    }
}