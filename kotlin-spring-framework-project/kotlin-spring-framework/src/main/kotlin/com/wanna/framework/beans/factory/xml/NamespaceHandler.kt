package com.wanna.framework.beans.factory.xml

import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.lang.Nullable
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * NamespaceHandler, 提供对于XML标签的解析成为一个Spring的BeanDefinition
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
interface NamespaceHandler {

    /**
     * 初始化NamespaceHandler
     */
    fun init()

    /**
     * 将一个Element去解析成为一个BeanDefinition
     *
     * @param element Element
     * @param parserContext ParserContext
     * @return 解析得到的BeanDefinition(解析失败return null)
     */
    @Nullable
    fun parse(element: Element, parserContext: ParserContext): BeanDefinition?

    /**
     * 对一个BeanDefinition去进行包装
     *
     * @param node Node
     * @param definition 原始的BeanDefinition
     * @param parserContext ParserContext
     * @return 包装之后的BeanDefinitionHolder(如果包装失败return null, 将会使用原始的BeanDefinitionHolder去进行使用)
     */
    @Nullable
    fun decorate(node: Node, definition: BeanDefinitionHolder, parserContext: ParserContext): BeanDefinitionHolder?
}