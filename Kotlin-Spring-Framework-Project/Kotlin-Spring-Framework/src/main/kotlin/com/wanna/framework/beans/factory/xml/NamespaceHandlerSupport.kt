package com.wanna.framework.beans.factory.xml

import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Attr
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * 为[NamespaceHandler]提供实现的抽象类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
abstract class NamespaceHandlerSupport : NamespaceHandler {

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)


    /**
     * BeanDefinitionParser列表，Key是标签名，Value是解析该标签所使用到的BeanDefinitionParser
     */
    private val parsers = HashMap<String, BeanDefinitionParser>()

    /**
     * BeanDefinitionDecorator列表
     */
    private val decorators = HashMap<String, BeanDefinitionDecorator>()

    /**
     * BeanDefinition的属性的包装器列表
     */
    private val attributeDecorators = HashMap<String, BeanDefinitionDecorator>()

    /**
     * 利用NamespaceHandler去解析Element成为BeanDefinition
     *
     * @param element Element
     * @param parserContext ParserContext
     * @return 解析到的BeanDefinition(没有解析到return null)
     */
    override fun parse(element: Element, parserContext: ParserContext) =
        findParserForElement(element, parserContext)?.parse(element, parserContext)

    /**
     * 根据Element的localName去找到需要去进行解析BeanDefinition的[BeanDefinitionParser]
     *
     * @param element Element
     * @param parserContext ParserContext
     * @return BeanDefinitionParser
     */
    private fun findParserForElement(element: Element, parserContext: ParserContext): BeanDefinitionParser? {
        val localName = parserContext.delegate.getLocalName(element)
        val beanDefinitionParser = parsers[localName]
        if (beanDefinitionParser != null) {
            return beanDefinitionParser
        }
        logger.error("无法为localName=[$localName]去找到合适的BeanDefinitionParser去进行解析")
        return null
    }

    /**
     * 利用NamespaceHandler去对原始的BeanDefinition去进行包装
     *
     * @param node Node
     * @param definition 原始BeanDefinition
     * @param parserContext ParserContext
     * @return 包装之后的BeanDefinitionHolder(如果return null，将会沿用原始的BeanDefinitionHolder)
     */
    override fun decorate(
        node: Node,
        definition: BeanDefinitionHolder,
        parserContext: ParserContext
    ) = findDecoratorForNode(node, parserContext)?.decorate(node, definition, parserContext)

    /**
     * 根据NodeType和它的localName去找到需要去进行包装的[BeanDefinitionDecorator]
     *
     * @param node Node
     * @param parserContext ParserContext
     * @return BeanDefinitionDecorator
     */
    private fun findDecoratorForNode(node: Node, parserContext: ParserContext): BeanDefinitionDecorator? {
        val localName = parserContext.delegate.getLocalName(node)
        val decorator = when (node) {
            is Element -> decorators[localName]
            is Attr -> attributeDecorators[localName]
            else -> throw IllegalStateException("不支持去处理这样的类型的Node[${node::class.java.name}]")
        }
        if (decorator != null) {
            return decorator
        }
        logger.error("无法为localName=[$localName]的Node去找到合适的BeanDefinitionDecorator去进行包装")
        return null
    }
}