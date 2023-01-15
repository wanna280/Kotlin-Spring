package com.wanna.framework.beans.factory.xml

import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import org.w3c.dom.Node

/**
 * BeanDefinition的包装器, 提供对于原始的BeanDefinition去进行包装的公民
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
fun interface BeanDefinitionDecorator {

    /**
     * 对给定的原始BeanDefinition去进行包装
     *
     * @param node Node
     * @param definition 原始的BeanDefinition
     * @param parserContext parserContext
     * @return 包装之后的BeanDefinitionHolder(不允许返回null)
     */
    fun decorate(node: Node, definition: BeanDefinitionHolder, parserContext: ParserContext): BeanDefinitionHolder
}