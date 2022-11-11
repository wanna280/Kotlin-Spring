package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.util.StringValueResolver

/**
 * 这是一个嵌入式的值解析器，支持去进行占位符的解析
 *
 * @see StringValueResolver
 */
open class EmbeddedValueResolver(private val beanFactory: ConfigurableListableBeanFactory) : StringValueResolver {

    override fun resolveStringValue(strVal: String): String? {
        return beanFactory.resolveEmbeddedValue(strVal)
    }
}