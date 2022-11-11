package com.wanna.framework.aop.framework

/**
 * 这是一个默认的AopProxy工厂，主要根据情况去决定采用何种方式去完成动态代理
 */
class DefaultAopProxyFactory : AopProxyFactory {
    override fun createAopProxy(config: AdvisedSupport): AopProxy {
        // 如果获取不到targetClass，那么抛出异常
        val targetClass = config.getTargetClass() ?: throw IllegalStateException("无法从TargetSource当中获取的代理的targetClass")

        // 如果有接口的话，那么尽可能地去使用Jdk动态代理；除非你明确了要使用Cglib去完成代理
        if (targetClass.interfaces.isNotEmpty()) {
            if (config.proxyTargetClass) {
                return CglibAopProxy(config)
            }
            return JdkDynamicAopProxy(config)
        }
        return CglibAopProxy(config)
    }
}