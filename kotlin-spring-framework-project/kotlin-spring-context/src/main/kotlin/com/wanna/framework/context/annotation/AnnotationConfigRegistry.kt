package com.wanna.framework.context.annotation

/**
 * 这是一个注解的配置类的注册中心, 提供配置类的注册; 实现这个接口的类, 可以使用注册配置类以及扫描包两种方式去给容器中注册Bean
 *
 * @see AnnotationConfigApplicationContext
 */
interface AnnotationConfigRegistry {
    /**
     * 将指定包下的全部配置类扫描并注册到容器当中
     */
    fun scan(vararg basePackages: String)

    /**
     * 将指定的配置类列表, 注册到容器当中
     */
    fun register(vararg componentClasses: Class<*>)
}