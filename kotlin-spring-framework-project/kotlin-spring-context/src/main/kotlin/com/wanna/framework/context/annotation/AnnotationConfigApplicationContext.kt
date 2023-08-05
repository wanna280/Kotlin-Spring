package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.BeanNameGenerator
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.annotation.AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR
import com.wanna.framework.context.support.AbstractApplicationContext
import com.wanna.framework.context.support.GenericApplicationContext
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 这是一个支持注解的处理的ApplicationContext,
 * * (1)可以通过注解的[BeanDefinitionReader]去完成配置类的注册;
 * * (2)可以通过[ClassPathBeanDefinitionScanner]去扫描指定的包下的所有配置类
 *
 * @see ClassPathBeanDefinitionScanner
 * @see AnnotatedBeanDefinitionReader
 * @see GenericApplicationContext
 * @see AbstractApplicationContext
 */
open class AnnotationConfigApplicationContext(beanFactory: DefaultListableBeanFactory) :
    GenericApplicationContext(beanFactory), AnnotationConfigRegistry {

    /**
     * 提供注解配置支持的BeanDefinition的Reader, 提供对于一个Spring的配置类的注册
     */
    private val reader = AnnotatedBeanDefinitionReader(this)

    /**
     * 类路径下的BeanDefinition的Scanner, 提供ComponentScan的功能
     */
    private val scanner = ClassPathBeanDefinitionScanner(this, true)

    /**
     * 无参构造器, 创建默认的Environment和BeanFactory;
     *
     * Note: 对于无参数构造器来说, 我们并不完成刷新工作, 需要使用者去自行完成ApplicationContext的刷新
     */
    constructor() : this(DefaultListableBeanFactory())

    /**
     * 注册配置类到容器中, 创建默认的Environment和BeanFactory, 并完成ApplicationContext的刷新
     *
     * @param componentClasses 需要去进行注册的配置类列表
     */
    constructor(vararg componentClasses: Class<*>) : this() {
        this.register(*componentClasses)
        this.refresh()
    }

    /**
     * 将指定包下的所有符合条件的全部配置类, 全部封装成为BeanDefinition全部注册到容器中, 并完成ApplicationContext的刷新
     *
     * @param basePackages 需要去进行扫描的包的列表
     */
    constructor(vararg basePackages: String) : this() {
        this.scan(*basePackages)
        this.refresh()
    }

    /**
     * 设置ApplicationContext的Environment, 给Scanner和Reader中的Environment都给替换掉了
     *
     * @param environment Environment
     */
    override fun setEnvironment(environment: ConfigurableEnvironment) {
        super.setEnvironment(environment)
        this.scanner.setEnvironment(environment)
        this.reader.setEnvironment(environment)
    }

    /**
     * 设置注册时, 要使用的BeanNameGenerator, 在进行ConfigurationClassPostProcessor, 支持从容器当中去进行获取BeanNameGenerator,
     * 也就是说, 通过ApplicationContext的setBeanNameGenerator, 可以替换全局的BeanNameGenerator
     *
     * @param beanNameGenerator BeanNameGenerator
     */
    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator) {
        this.reader.setBeanNameGenerator(beanNameGenerator)
        this.scanner.setBeanNameGenerator(beanNameGenerator)

        // 往BeanFactory当中去注册单实例的Bean, 指定beanName, 后续可以通过beanName, 获取到这个BeanNameGenerator
        getBeanFactory().registerSingleton(CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator)
    }

    /**
     * 使用批量的方式去注册很多个配置类到容器当中
     *
     * @param componentClasses 要注册的配置类列表
     */
    override fun register(vararg componentClasses: Class<*>) {
        val register = this.getApplicationStartup()
            .start("spring.context.component-class.register") // start register
            .tag("class", componentClasses.contentToString())
        this.reader.registerBean(*componentClasses)  // do register
        register.end()  // end
    }

    /**
     * 扫描指定的包, 并对自定的包下的所有配置类去进行扫描
     *
     * @param basePackages 要扫描的包的列表
     */
    override fun scan(vararg basePackages: String) {
        val scan = this.getApplicationStartup()
            .start("spring.context.base-packages.scan") // start scan
            .tag("class", basePackages.contentToString())
        this.scanner.scan(*basePackages)
        scan.end()  // end
    }
}