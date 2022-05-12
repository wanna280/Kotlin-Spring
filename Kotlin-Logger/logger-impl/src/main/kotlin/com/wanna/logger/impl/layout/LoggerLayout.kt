package com.wanna.logger.impl.layout

interface LoggerLayout<E> {
    fun doLayout(e: E) : String
}