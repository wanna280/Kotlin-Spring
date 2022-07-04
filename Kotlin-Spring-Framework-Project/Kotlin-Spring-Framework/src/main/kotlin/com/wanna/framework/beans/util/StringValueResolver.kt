package com.wanna.framework.beans.util

import com.wanna.framework.lang.Nullable

/**
 * 这是一个字符串值的解析器，比如"${}"表达式的解析
 */
interface StringValueResolver {

    @Nullable
    fun resolveStringValue(strVal: String): String?
}