package com.wanna.framework.validation.beanvalidation

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.core.ParameterNameDiscoverer
import com.wanna.framework.core.io.Resource
import java.io.IOException
import javax.validation.*
import javax.validation.spi.ValidationProvider

/**
 * 基于本地的ValidatorFactory([javax.validation.ValidatorFactory])去实现的SpringValidatorAdapter，
 * 可以通过这个类去配置自定义原生的[javax.validation.Validator]的各个配置信息；
 *
 * 为SpringMVC当中的`@ModelAttribute`注解和`@RequestBody`注解当中的参数检验的实现提供支持；
 *
 * @see SpringValidatorAdapter
 *
 */
open class LocalValidatorFactoryBean : SpringValidatorAdapter(), InitializingBean, ApplicationContextAware {

    /**
     * ValidatorFactory
     */
    private var validatorFactory: ValidatorFactory? = null

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    /**
     * ValidationProvider Class
     */
    private var providerClass: Class<out ValidationProvider<*>>? = null

    /**
     * MessageInterpolator
     */
    private var messageInterpolator: MessageInterpolator? = null

    /**
     * Mapping Locations
     */
    private var mappingLocations: Array<Resource>? = null

    /**
     * ConstraintValidatorFactory
     */
    private var constraintValidatorFactory: ConstraintValidatorFactory? = null

    /**
     * TraversableResolver
     */
    private var traversableResolver: TraversableResolver? = null

    /**
     * ValidationProvider Resolver
     */
    private var validationProviderResolver: ValidationProviderResolver? = null

    /**
     * ValidationPropertyMap
     */
    private var validationPropertyMap = HashMap<String, String>()

    /**
     * Spring的参数名发现器，提供对于构造器/方法的参数名的获取
     */
    private var parameterNameDiscoverer: ParameterNameDiscoverer? = DefaultParameterNameDiscoverer()

    @Suppress("UNCHECKED_CAST")
    override fun afterPropertiesSet() {
        val configuration: Configuration<*>

        // 初始化Provider&ValidationProviderResolver
        if (providerClass != null) {
            var bootstrap = Validation.byProvider(providerClass!! as Class<Nothing>)
            if (validationProviderResolver != null) {
                bootstrap = bootstrap.providerResolver(validationProviderResolver)
            }
            configuration = bootstrap.configure()
        } else {
            var bootstrap = Validation.byDefaultProvider()
            if (validationProviderResolver != null) {
                bootstrap = bootstrap.providerResolver(validationProviderResolver)
            }
            configuration = bootstrap.configure()
        }

        // 初始化MessageInterpolator
        var messageInterpolator = messageInterpolator
        if (messageInterpolator == null) {
            messageInterpolator = configuration.defaultMessageInterpolator
        }
        configuration.messageInterpolator(messageInterpolator)

        // 初始化TraversableResolver
        if (this.traversableResolver != null) {
            configuration.traversableResolver(this.traversableResolver!!)
        }

        // 初始化ConstraintValidatorFactory
        var constraintValidatorFactory = constraintValidatorFactory
        if (constraintValidatorFactory == null && applicationContext != null) {
            constraintValidatorFactory =
                SpringConstraintValidatorFactory(applicationContext!!.getAutowireCapableBeanFactory())
        }
        if (constraintValidatorFactory != null) {
            configuration.constraintValidatorFactory(constraintValidatorFactory)
        }

        // 配置参数名的Provider(将Spring的ParameterNameDiscoverer去转换成为javax.validation.ParameterNameProvider)
        configureParameterNameProviderIfPossible(configuration)

        // add MappingLocations
        this.mappingLocations?.forEach {
            try {
                configuration.addMapping(it.getInputStream())
            } catch (ex: IOException) {
                throw IllegalStateException("无法找到资源[$it]", ex)
            }
        }

        // 将ValidationPropertyMap当中的元素全部添加到Configuration当中
        this.validationPropertyMap.forEach(configuration::addProperty)

        // 模板方法，交给子类去进行实现，对Configuration去进行更多自定义操作
        postProcessConfiguration(configuration)

        // build ValidatorFactory&Validator
        this.validatorFactory = configuration.buildValidatorFactory()
        setTargetValidator(this.validatorFactory!!.validator)
    }

    /**
     * 如果必要的话，对Configuration的ParameterNameProvider去进行配置
     *
     * @param configuration [javax.validation.Configuration]
     */
    private fun configureParameterNameProviderIfPossible(configuration: Configuration<*>) {

    }

    /**
     * 提供对于[Configuration]的自定义工作，交给子类去进行实现
     *
     * @param configuration Configuration
     */
    protected open fun postProcessConfiguration(configuration: Configuration<*>) {

    }

    /**
     * 设置ApplicationContext
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    open fun setParameterNameDiscoverer(parameterNameDiscoverer: ParameterNameDiscoverer?) {
        this.parameterNameDiscoverer = parameterNameDiscoverer
    }

    open fun setValidatorPropertyMap(propertyMap: Map<String, String>?) {
        propertyMap?.forEach(this.validationPropertyMap::put)
    }

    open fun setProviderClass(providerClass: Class<out ValidationProvider<*>>) {
        this.providerClass = providerClass
    }

    open fun setMessageInterpolator(messageInterpolator: MessageInterpolator?) {
        this.messageInterpolator = messageInterpolator
    }

    open fun setTraversableResolver(traversableResolver: TraversableResolver?) {
        this.traversableResolver = traversableResolver
    }

    open fun setValidationProviderResolver(validationProviderResolver: ValidationProviderResolver?) {
        this.validationProviderResolver = validationProviderResolver
    }

    open fun setConstraintValidatorFactory(constraintValidatorFactory: ConstraintValidatorFactory?) {
        this.constraintValidatorFactory = constraintValidatorFactory
    }

    open fun setMappingLocations(vararg mappingLocations: Resource) {
        this.mappingLocations = arrayOf(*mappingLocations)
    }
}