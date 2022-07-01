package com.wanna.framework.context

import com.wanna.framework.beans.util.StringValueResolver


/**
 * 这是一个嵌入式的值解析器的Aware接口，可以给Bean注入嵌入式的值解析器
 *
 * @see ApplicationContextAware
 */
interface EmbeddedValueResolverAware {
    fun setEmbeddedValueResolver(resolver: StringValueResolver)
}