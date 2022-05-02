package com.wanna.boot.web.reactive.context

import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.annotation.AnnotatedBeanDefinitionReader
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.annotation.ClassPathBeanDefinitionScanner
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.util.AnnotationConfigUtils

/**
 * 这是一个基于ReactiveWeb环境下的支持注解的ApplicationContext，它相比于AnnotationConfigApplicationContext，新增了ReactiveWebServer功能；
 * 在启动过程当中，会自动从容器当中获取WebServerFactory，并往ApplicationContext当中去注册一个Lifycycle，从而去实现WebServer的启动
 *
 * @see com.wanna.framework.context.support.AbstractApplicationContext
 * @see com.wanna.framework.context.support.GenericApplicationContext
 * @see com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
 */
open class AnnotationConfigReactiveWebServerApplicationContext(_beanFactory: DefaultListableBeanFactory) :
    ReactiveWebServerApplicationContext(_beanFactory) {
    // 注解的BeanDefinition的Reader
    private var reader: AnnotatedBeanDefinitionReader = AnnotatedBeanDefinitionReader(this)

    // 类路径下的BeanDefinition的Scanner
    private var scanner: ClassPathBeanDefinitionScanner = ClassPathBeanDefinitionScanner(this)

    /**
     * 无参构造器，创建默认的Environment和BeanFactory，但是并不完成刷新工作，使用者自行完成ApplicationContext的刷新
     */
    constructor() : this(DefaultListableBeanFactory())

    /**
     * 注册配置类到容器中，创建默认的Environment和BeanFactory，并完成ApplicationContext的刷新
     */
    constructor(vararg clazzes: Class<*>) : this() {
        this.register(*clazzes)
        this.refresh()
    }

    /**
     * 将指定包下的所有符合条件的全部配置类，全部封装成为BeanDefinition全部注册到容器中，并完成ApplicationContext的刷新
     */
    constructor(vararg packages: String) : this() {
        this.scanner.scan(*packages)
        this.refresh()
    }

    /**
     * 注册一个配置类到容器当中，成为一个Bean
     */
    open fun register(clazz: Class<*>) {
        reader.registerBean(clazz)
    }

    /**
     * 设置ApplicationContext的Environment，给Scanner和Reader中的Environment都给替换掉了
     */
    override fun setEnvironment(environment: ConfigurableEnvironment) {
        super.setEnvironment(environment)
        this.scanner.setEnvironment(environment)
        this.reader.setEnvironment(environment)
    }

    /**
     * 设置注册时，要使用的BeanNameGenerator，在进行ConfigurationClassPostProcessor，支持从容器当中去进行获取BeanNameGenerator，
     * 也就是说，通过ApplicationContext的setBeanNameGenerator，可以替换全局的BeanNameGenerator
     */
    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator) {
        this.reader.setBeanNameGenerator(beanNameGenerator)
        this.scanner.setBeanNameGenerator(beanNameGenerator)

        // 往BeanFactory当中去注册单实例的Bean，指定beanName，后续可以通过beanName，获取到这个BeanNameGenerator
        getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator)
    }

    /**
     * 注册很多个配置类
     */
    open fun register(vararg clazzes: Class<*>) {
        clazzes.forEach(this::register)
    }
}