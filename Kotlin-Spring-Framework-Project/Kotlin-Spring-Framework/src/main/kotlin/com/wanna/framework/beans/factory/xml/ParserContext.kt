package com.wanna.framework.beans.factory.xml

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * Parser上下文，会将这个参数交给[NamespaceHandler]，让它可以去进行自定义的处理
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 * @see NamespaceHandler
 * @see NamespaceHandlerResolver
 *
 * @param readerContext ReaderContext
 * @param delegate delegate
 * @param containingBd 已经存在的BeanDefinition(如果有的话)
 */
class ParserContext(
    val readerContext: XmlReaderContext,
    val delegate: BeanDefinitionParserDelegate,
    val containingBd: BeanDefinition?
) {

}