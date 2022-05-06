package com.wanna.framework.context.annotation

/**
 * 这是一个注解的配置类的注册中心，提供配置类的注册；
 * 可以使用注册配置类以及扫描包两种方式去进行注册
 */
interface AnnotationConfigRegistry {
    fun scan(vararg basePackages: String)

    fun register(vararg componentClasses:Class<*>)
}