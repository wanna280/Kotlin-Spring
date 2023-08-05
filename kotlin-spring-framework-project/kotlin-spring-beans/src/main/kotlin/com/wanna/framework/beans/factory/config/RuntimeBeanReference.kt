package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.beans.factory.support.BeanDefinitionValueResolver
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.AbstractAutowireCapableBeanFactory

/**
 * 运行时去进行解析的Bean的引用, 暂时设置为beanName, 后期支持交给BeanFactory去进行自动解析
 *
 * @param beanName beanName
 * @see AbstractAutowireCapableBeanFactory.applyPropertyValues
 * @see BeanDefinitionValueResolver.resolveValueIfNecessary
 * @see BeanDefinition.getPropertyValues
 * @see MutablePropertyValues.addPropertyValue
 */
open class RuntimeBeanReference(private val beanName: String) : BeanReference {

    private var source: Any? = null

    override fun getBeanName() = beanName

    override fun getSource(): Any? = source

    open fun setSource(source:Any?) {
        this.source = source
    }
}