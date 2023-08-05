package com.wanna.framework.context.support

import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.config.PlaceholderConfigurerSupport
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.environment.*
import java.util.*

/**
 * 它是一个基于PropertySources的处理占位符的处理器
 *
 * @author jianchao.jia
 * @version 1.0
 */
open class PropertySourcesPlaceholderConfigurer : EnvironmentAware, PlaceholderConfigurerSupport() {
    companion object {
        const val LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME = "localProperties"
        const val ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME = "environmentProperties"
    }


    /**
     * Environment
     */
    private var environment: Environment? = null

    /**
     * PropertySources
     */
    private var propertySources: MutablePropertySources? = null

    /**
     * 获取已经被应用的PropertySources
     */
    private var appliedPropertySources: MutablePropertySources? = null

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    /**
     * 设置PropertySources
     *
     * @param propertySources PropertySources
     */
    open fun setPropertySources(propertySources: PropertySources) {
        this.propertySources = MutablePropertySources(propertySources)
    }

    /**
     * 因为我们直接重写了postProcessBeanFactory方法, 因此这个方法我们用不上
     */
    override fun processProperties(beanFactory: ConfigurableListableBeanFactory, properties: Properties) {
        throw UnsupportedOperationException("考虑使用processProperties(ConfigurableListableBeanFactory, ConfigurablePropertyResolver)方法去进行使用")
    }

    /**
     * 我们直接重写父类的postProcessBeanFactory方法去进行自定义操作
     *
     * @param beanFactory BeanFactory
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

        // 构建出来一个PropertySources, 去包装Environment去作为PropertySourcesPropertyResolver最终再去实现占位符的解析
        var propertySources: MutablePropertySources? = this.propertySources
        if (propertySources == null) {
            propertySources = MutablePropertySources()

            // 1.添加Environment去作为PropertySource
            if (this.environment != null) {
                propertySources.addLast(object :
                    PropertySource<Environment>(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, environment!!) {
                    override fun getProperty(name: String) = this.source.getProperty(name)
                })
            }

            // 2.添加本地的Properties和Resource去作为PropertySource
            val propertySource = PropertiesPropertySource(LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME, mergeProperties())
            if (localOverride) {
                propertySources.addFirst(propertySource)
            } else {
                propertySources.addLast(propertySource)
            }
        }

        this.processProperties(beanFactory, PropertySourcesPropertyResolver(propertySources))
        this.appliedPropertySources = propertySources
    }

    /**
     * 将PropertyResolver去包装成为一个StringValueResolver并添加到BeanFactory当中
     *
     * @param beanFactory BeanFactory
     * @param propertyResolver PropertyResolver
     */
    protected open fun processProperties(
        beanFactory: ConfigurableListableBeanFactory,
        propertyResolver: ConfigurablePropertyResolver
    ) {
        propertyResolver.setPlaceholderPrefix(placeholderPrefix)
        propertyResolver.setPlaceholderSuffix(placeholderSuffix)
        propertyResolver.setValueSeparator(valueSeparator)
        val valueResolver = StringValueResolver { strVal ->
            val resolved = propertyResolver.resolvePlaceholders(strVal)
            if (resolved == nullValue) null else resolved
        }
        this.doProcessProperties(beanFactory, valueResolver)
    }

    /**
     * 获取已经应用的PropertySources
     *
     * @return AppliedPropertySources
     * @throws IllegalStateException 如果还没生成过可以被应用的PropertySources的话
     */
    @Throws(IllegalStateException::class)
    open fun getAppliedPropertySources(): MutablePropertySources =
        this.appliedPropertySources ?: throw IllegalStateException("没有可以去进行应用的PropertySources")
}