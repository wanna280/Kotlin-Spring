package com.wanna.framework.beans.util

import com.wanna.framework.lang.Nullable

/**
 * 这是一个字符串值的解析器，比如"${}"表达式的解析
 *
 * @see com.wanna.framework.beans.factory.config.EmbeddedValueResolver
 */
interface StringValueResolver {

    /**
     * 去对给定的字符串去进行字符串的解析，比如对"${}"去进行占位符的替换的解析
     *
     * @param strVal 待解析的字符串
     * @return 解析完成的字符串
     */
    @Nullable
    fun resolveStringValue(strVal: String): String?
}