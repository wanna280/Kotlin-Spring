package com.wanna.framework.context

import com.wanna.framework.beans.RootBeanDefinition
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.StandardEnvironment

/**
 * 这是一个支持注解的处理的ApplicationContext
 */
class AnnotationConfigApplicationContext(
    override var environment: ConfigurableEnvironment?,
    beanFactory: DefaultListableBeanFactory
) :
    GenericApplicationContext(beanFactory) {

    // 无参数构造器，啥都创建默认的
    constructor() : this(StandardEnvironment(), DefaultListableBeanFactory()) {

    }

    // 注册配置类到容器中，创建默认的Environment和BeanFactory
    constructor(vararg clazzes: Class<*>) : this() {
        register(clazzes.toList())
    }

    /**
     * 注册一个配置类
     */
    fun register(clazz: Class<*>) {
        clazz.let {
            val name = it.simpleName
            if (beanFactory != null) beanFactory!!.registerBeanDefinition(
                name,
                RootBeanDefinition(name, it)
            ) else throw IllegalStateException("Bean factory don't exists")
        }
    }

    /**
     * 注册很多个配置类
     */
    fun register(clazzes: List<Class<*>>) {
        clazzes.forEach {
            register(it)
        }
    }
}