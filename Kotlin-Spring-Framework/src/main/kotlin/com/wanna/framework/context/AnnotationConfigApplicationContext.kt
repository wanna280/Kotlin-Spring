package com.wanna.framework.context

import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.util.ConfigurationClassUtils
import com.wanna.framework.context.annotations.AnnotatedBeanDefinitionReader
import com.wanna.framework.context.annotations.BeanNameGenerator
import com.wanna.framework.context.annotations.ClassPathBeanDefinitionScanner
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.util.AnnotationConfigUtils

/**
 * 这是一个支持注解的处理的ApplicationContext
 */
open class AnnotationConfigApplicationContext(
    _environment: ConfigurableEnvironment,
    beanFactory: DefaultListableBeanFactory
) : GenericApplicationContext(beanFactory) {

    private var environment: ConfigurableEnvironment? = _environment

    // 注解的BeanDefinition的Reader
    private var reader: AnnotatedBeanDefinitionReader = AnnotatedBeanDefinitionReader(this)

    // 类路径下的BeanDefinition的Scanner
    private var scanner: ClassPathBeanDefinitionScanner = ClassPathBeanDefinitionScanner(this)

    /**
     * 无参构造器，创建默认的Environment和BeanFactory，但是并不完成刷新工作，使用者自行完成ApplicationContext的刷新
     */
    constructor() : this(StandardEnvironment(), DefaultListableBeanFactory()) {

    }

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
        this.environment = environment
        this.scanner.environment = environment
        this.reader.environment = environment
    }

    /**
     * 设置注册时，要使用的BeanNameGenerator，在进行ConfigurationClassPostProcessor，支持从容器当中去进行获取BeanNameGenerator，
     * 也就是说，通过ApplicationContext的setBeanNameGenerator，可以替换全局的BeanNameGenerator
     */
    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator) {
        this.reader.beanNameGenerator = beanNameGenerator
        this.scanner.beanNameGenerator = beanNameGenerator

        // 往BeanFactory当中去注册单实例的Bean，指定beanName，后续可以通过beanName，获取到这个BeanNameGenerator
        getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator)
    }

    /**
     * 注册很多个配置类
     */
    open fun register(vararg clazzes: Class<*>) {
        clazzes.forEach(this::register)
    }

    override fun getEnvironment(): Environment {
        return environment as Environment
    }
}