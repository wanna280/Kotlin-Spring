package com.wanna.framework.core.convert.converter

/**
 * 这是一个Converter，用来完成类型的转换(从S->T)
 *
 * @param S 源类型
 * @param T 目标类型
 */
fun interface Converter<S : Any, T : Any> {
    fun convert(source: S?): T?
}