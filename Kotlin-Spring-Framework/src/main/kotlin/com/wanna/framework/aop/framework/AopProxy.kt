package com.wanna.framework.aop.framework

/**
 * 这是对Spring当中的Aop代理进行的一层抽象，具体实现包括Jdk/Cglib的AopProxy；
 * 可以使用AopProxyFactory去完成Aop代理的生成
 *
 * @see JdkDynamicAopProxy
 * @see CglibAopProxy
 * @see AopProxyFactory
 * @see DefaultAopProxyFactory
 */
interface AopProxy {
    /**
     * 使用默认的类加载去实现代理类的类加载
     */
    fun getProxy(): Any

    /**
     * 使用给定的类加载器去实现代理类的类加载
     * @param classLoader 给定的类加载器
     */
    fun getProxy(classLoader: ClassLoader?): Any
}