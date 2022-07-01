package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.ObjectFactory

/**
 * 这是一个自定义的Scope，支持从自定义的Scope当中去获取对象，每个Scope当中维护了自己作用域内的对象
 */
interface Scope {
    /**
     * 从Scope作用域内获取对象
     *
     * @param beanName beanName
     * @param factory 创建Bean的回调函数(Spring BeanFactory的createBean)
     */
    fun get(beanName: String, factory: ObjectFactory<*>) : Any

    /**
     * 给Scope内注册DisposableBean的回调，当对Bean去进行destroy时，需要完成的回调处理工作
     *
     * @param name beanName
     * @param callback 回调方法(Runnable)
     */
    fun registerDestructionCallback(name: String, callback: Runnable)

    /**
     * 给定一个name，去移除在Scope的统一管理下的Bean
     *
     * @param name 想要移除的name
     * @return 移除的对象
     */
    fun remove(name: String): Any?
}