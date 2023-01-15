package com.wanna.framework.beans.factory.config

import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.PriorityOrdered
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.support.PropertiesLoaderSupport
import java.util.*

/**
 * 提供了对属性资源的配置功能, 父类[PropertiesLoaderSupport]当中已经提供了对于XML配置文件和[Resource]的加载;
 * 在这个类当中, 我们通过[BeanFactoryPostProcessor]的方式, 交给用户一些去对最终的[Properties]的自定义的机会
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/22
 */
abstract class PropertyResourceConfigurer : PropertiesLoaderSupport(), BeanFactoryPostProcessor, PriorityOrdered {

    /**
     * 配置优先级
     */
    private var order = Ordered.ORDER_LOWEST

    /**
     * 自定义当前Bean的优先级
     *
     * @param order 优先级
     */
    open fun setOrder(order: Int) {
        this.order = order
    }

    override fun getOrder() = this.order

    /**
     * 对BeanFactory去进行后置处理
     *
     * @param beanFactory BeanFactory
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

        // 1.merge Resource&Properties
        val properties = mergeProperties()

        // 2.对Properties去进行转换
        convertProperties(properties)

        // 3.对Properties去进行后置处理
        processProperties(beanFactory, properties)
    }

    /**
     * 如果必要的话对Properties当中的属性值去进行转换
     *
     * @param properties 待转换的Properties
     */
    protected open fun convertProperties(properties: Properties) {
        val propertyNames = properties.propertyNames()
        propertyNames.asIterator().forEach {
            if (it is String) {
                val propertyValue = properties.getProperty(it)
                val convertedValue = convertProperty(it, propertyValue)
                if (!Objects.equals(propertyValue, convertedValue)) {
                    properties.setProperty(it, convertedValue)
                }
            }
        }
    }

    /**
     * 对一个属性(能获取到propertyName和propertyValue)去进行具体的处理
     *
     * @param propertyName propertyName
     * @param propertyValue propertyValue
     * @return 经过转换之后的属性值, 将会替换原始的值
     */
    protected open fun convertProperty(propertyName: String, propertyValue: String?): String? =
        convertPropertyValue(propertyValue)

    /**
     * 对一个属性值(无法获取到propertyName, 只能获取到propertValue)去进行具体的处理
     *
     * @param propertyValue propertyValue
     * @return 经过转换之后的属性值, 将会替换原始的值
     */
    protected open fun convertPropertyValue(propertyValue: String?): String? = propertyValue

    /**
     * 对Properties去进行更多的后置处理
     *
     * @param beanFactory 正在处理的BeanFactory
     * @param properties 待后置处理的Properties
     */
    protected abstract fun processProperties(beanFactory: ConfigurableListableBeanFactory, properties: Properties)
}