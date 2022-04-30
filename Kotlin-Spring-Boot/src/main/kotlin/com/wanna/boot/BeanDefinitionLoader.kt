package com.wanna.boot

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.context.annotation.AnnotatedBeanDefinitionReader
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 这是一个BeanDefinition的Loader，负责完成BeanDefinition的加载
 *
 * @see AnnotatedBeanDefinitionReader
 */
open class BeanDefinitionLoader(private val registry: BeanDefinitionRegistry, private val sources: Array<*>) {

    private var beanNameGenerator: BeanNameGenerator? = null

    private var environment: ConfigurableEnvironment? = null

    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator) {
        this.beanNameGenerator = beanNameGenerator
        this.reader.beanNameGenerator = beanNameGenerator
    }

    open fun setEnvironment(environment: ConfigurableEnvironment) {
        this.environment = environment
        this.reader.environment = environment
    }

    // 这是一个BeanDefinitionReader，负责注册配置类到容器当中
    private val reader = AnnotatedBeanDefinitionReader(registry)

    /**
     * 加载一个配置类，使用DefinitionReader去进行注册即可
     */
    protected open fun load(clazz: Class<*>) {
        reader.registerBean(clazz)
    }

    open fun load() {
        sources.forEach {
            if (it is Class<*>) {
                load(it)
            }
        }
    }
}