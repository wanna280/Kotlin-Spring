package com.wanna.framework.context

/**
 * 这是一个字符串值的解析器，比如"${}"表达式的解析
 */
interface StringValueResolver {
    fun resolveStringValue(strVal: String): String?
}