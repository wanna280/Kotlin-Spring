package com.wanna.boot.context.properties

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.util.ClassUtils
import org.slf4j.LoggerFactory

/**
 * 它是一个ValueObject(值对象)的ConfigurationProperties的BeanDefinition，主要提供Supplier去进行ConfigurationProperties绑定；
 * 它为@ConstructorBinding注解提供支持，在运行时去使用Supplier的方式去进行实例化，而不是使用Spring的实例化策略去进行实例化；
 * 使用@ConstructorBinding注解的，将会按照构造器去对Bean的属性去进行填充，而不是使用setter去对Bean的属性去进行填充
 *
 * @see ConstructorBinding
 * @see ConfigurationPropertiesBean.BindMethod
 *
 * @param beanClass beanClass
 * @param beanName beanName
 * @param beanFactory BeanFactory
 */
class ConfigurationPropertiesValueObjectBeanDefinition(
    private val beanFactory: BeanFactory,
    beanClass: Class<*>,
    private val beanName: String
) : GenericBeanDefinition() {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ConfigurationPropertiesBindingPostProcessor::class.java)
    }

    init {
        setBeanClass(beanClass)  // setBeanClass
        setInstanceSupplier(this::createBean)  // set InstanceSupplier
    }

    /**
     * 提供运行时去实例化ConfigurationPropertiesBean的方式，支持去处理ConstructorBinding注解
     *
     * @see ConfigurationPropertiesBean.forValueObject
     * @see ConfigurationPropertiesBinder.get
     * @throws IllegalStateException 如果类上找不到@ConfigurationProperties注解
     */
    private fun createBean(): Any {
        val propertiesBean = ConfigurationPropertiesBean.forValueObject(getBeanClass()!!, this.beanName)
        val binder = ConfigurationPropertiesBinder.get(beanFactory)
        try {
            return binder.bindOrCreate(propertiesBean)!!
        } catch (ex: Exception) {
            logger.error(
                "bind @ConstructorBinding beanName={}, beanClass={} for Value Object Error",
                beanName, getBeanClassName(), ex
            )
            throw ConfigurationPropertiesBindException(bean = propertiesBean, cause = ex)
        }
    }

}