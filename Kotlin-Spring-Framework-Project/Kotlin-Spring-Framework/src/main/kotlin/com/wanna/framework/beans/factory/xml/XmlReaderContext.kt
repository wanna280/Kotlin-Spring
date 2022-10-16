package com.wanna.framework.beans.factory.xml

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.Resource

/**
 * XmlReader的上下文信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
class XmlReaderContext(
    val xmlBeanDefinitionReader: XmlBeanDefinitionReader,
    val namespaceHandlerResolver: NamespaceHandlerResolver,
    val resource: Resource
) {

    /**
     * 获取BeanClassLoader
     *
     * @return BeanClassLoader
     */
    fun getBeanClassLoader(): ClassLoader? = this.xmlBeanDefinitionReader.getBeanClassLoader()

    /**
     * 获取BeanDefinitionRegistry
     *
     * @return BeanDefinitionRegistry
     */
    fun getRegistry(): BeanDefinitionRegistry = this.xmlBeanDefinitionReader.getRegistry()

    /**
     * 获取Environment
     *
     * @return Environment
     */
    fun getEnvironment(): Environment = this.xmlBeanDefinitionReader.getEnvironment()

    /**
     * 为BeanDefinition去生成BeanName
     *
     * @param beanDefinition beanDefinition
     * @return 生成的beanName
     */
    fun generateBeanName(beanDefinition: BeanDefinition): String =
        this.xmlBeanDefinitionReader.getBeanNameGenerator().generateBeanName(beanDefinition, getRegistry())

}