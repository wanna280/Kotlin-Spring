package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.ObjectFactory

/**
 * 这是一个自定义的Scope，支持从自定义的Scope当中去获取对象，每个Scope当中维护了自己作用域内的对象
 */
interface Scope {
    /**
     * 从Scope作用域内获取对象
     * @param beanName beanName
     * @param factory 创建Bean的回调函数
     */
    fun get(beanName: String, factory: ObjectFactory<*>)
}