package com.wanna.framework.aop.framework

/**
 * 在这个类当中，对代理的配置去进行抽象了一层，在Spring当中凡是需要完成代理的配置，都会继承这个类，
 * 这个类当中为代理提供了相关的配置信息
 */
open class ProxyConfig : java.io.Serializable {
    // 是否代理目标类？设为true时，使用CGLIB去进行代理；设置false时，如果有接口采用JDK动态代理
    var proxyTargetClass = false

    // 是否要对Advisor列表去进行冻结？如果是的话，那么Advisor列表将不允许被修改，一旦修改立马抛异常
    var frozen = false

    /**
     * 从某个也实现了ProxyConfig的类当中，拷贝它对代理的配置属性
     * @param config 要从哪个ProxyConfig对象去进行拷贝
     */
    open fun copyFrom(config: ProxyConfig) {
        this.proxyTargetClass = config.proxyTargetClass
    }
}