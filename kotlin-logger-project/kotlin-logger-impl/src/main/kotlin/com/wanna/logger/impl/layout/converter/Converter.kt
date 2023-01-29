package com.wanna.logger.impl.layout.converter

interface Converter<E> {
    fun convert(expression: String, event: E): String
}