package com.wanna.framework.context

import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.annotations.AnnotatedBeanDefinitionReader
import com.wanna.framework.context.annotations.ClassPathBeanDefinitionScanner
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.StandardEnvironment

/**
 * 这是一个支持注解的处理的ApplicationContext
 */
class AnnotationConfigApplicationContext
private constructor(
    override var environment: ConfigurableEnvironment?,
    beanFactory: DefaultListableBeanFactory
) :
    GenericApplicationContext(beanFactory) {

    // 注解的BeanDefinition的Reader
    var reader: AnnotatedBeanDefinitionReader = AnnotatedBeanDefinitionReader(this)

    // 类路径下的BeanDefinition的Scanner
    var scanner: ClassPathBeanDefinitionScanner = ClassPathBeanDefinitionScanner(this)

    /**
     * 无参构造器，创建默认的Environment和BeanFactory
     */
    constructor() : this(StandardEnvironment(), DefaultListableBeanFactory()) {

    }

    // 注册配置类到容器中，创建默认的Environment和BeanFactory
    constructor(vararg clazzes: Class<*>) : this() {
        register(*clazzes)
        refresh()  // 完成ApplicationContext的刷新工作
    }

    /**
     * 注册一个配置类
     */
    fun register(clazz: Class<*>) {
        clazz.let {
            val name = it.simpleName
            val beanFactory = getBeanFactory()
            beanFactory.registerBeanDefinition(
                name,
                RootBeanDefinition(it)
            )
        }
    }

    /**
     * 注册很多个配置类
     */
    fun register(vararg clazzes: Class<*>) {
        clazzes.forEach(this::register)
    }

    override fun getEnvironment(): Environment {
        return environment as Environment
    }
}