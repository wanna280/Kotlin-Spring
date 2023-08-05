package com.wanna.framework.aop.framework

/**
 * 这是一个为ProxyCreator提供Aop代理的支持的类, 它集成了AopProxyFactory, 可以完成AOP的动态代理的生成
 */
open class ProxyCreatorSupport : AdvisedSupport() {

    // 创建Aop动态代理的ProxyFactory, 可以决定使用Jdk/Cglib去进行代理对象的生成
    var aopProxyFactory: AopProxyFactory = DefaultAopProxyFactory()
}