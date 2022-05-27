package com.wanna.framework.context.support

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.core.environment.Environment

/**
 * 它是一个处理占位符的处理器
 */
open class PropertySourcesPlaceholderConfigurer : EnvironmentAware, BeanFactoryAware, BeanFactoryPostProcessor {

    private lateinit var environment: Environment

    private lateinit  var beanFactory: BeanFactory

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    /**
     * 添加一个嵌入式的值解析器到beanFactory当中
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        beanFactory.addEmbeddedValueResolver(object : StringValueResolver {
            override fun resolveStringValue(strVal: String): String? {
                return this@PropertySourcesPlaceholderConfigurer.environment!!.resolvePlaceholders(strVal)
            }
        })
    }
}