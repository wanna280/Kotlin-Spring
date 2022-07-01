package com.wanna.framework.core.convert.converter

/**
 * 这是一个Converter，用来完成类型的转换(从S->T)
 *
 * @param S 源类型
 * @param T 目标类型
 */
interface Converter<S, T> {
    fun convert(source: S?): T?
}