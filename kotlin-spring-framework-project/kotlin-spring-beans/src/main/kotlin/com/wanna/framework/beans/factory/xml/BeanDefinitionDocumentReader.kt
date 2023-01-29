package com.wanna.framework.beans.factory.xml

import org.w3c.dom.Document

/**
 * BeanDefinition的DocumentReader, 负责将一个Document去转换成为一个BeanDefinition
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 * @see XmlBeanDefinitionReader
 */
fun interface BeanDefinitionDocumentReader {

    /**
     * 将W3C的Document解析成为BeanDefinition, 并完成注册功能
     *
     * @param document W3C的Document
     * @param readerContext 上下文信息(NamespaceHandlerResolver/Resource/XmlBeanDefinitionReader...)
     */
    fun registerBeanDefinitions(document: Document, readerContext: XmlReaderContext)
}