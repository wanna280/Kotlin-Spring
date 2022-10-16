package com.wanna.framework.beans.factory.xml

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.lang.Nullable
import org.w3c.dom.Element

/**
 * BeanDefinition的Parser
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
interface BeanDefinitionParser {

    /**
     * 将给定元素去解析成为BeanDefinition
     *
     * @param element element
     * @param parserContext ParserContext上下文信息
     * @return 解析出来的BeanDefinition(解析失败return null)
     */
    @Nullable
    fun parse(element: Element, parserContext: ParserContext): BeanDefinition?
}