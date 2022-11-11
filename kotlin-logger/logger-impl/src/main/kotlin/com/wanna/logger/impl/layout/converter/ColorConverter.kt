package com.wanna.logger.impl.layout.converter

import com.wanna.logger.impl.event.ILoggingEvent

class ColorConverter : Converter<ILoggingEvent> {

    /**
     * 日志的颜色渲染的格式为%green(xxx)，但是ansi的颜色渲染格式为@|green |@，需要去进行格式的转换
     */
    override fun convert(expression: String, event: ILoggingEvent): String {
        val builder = StringBuilder(expression)

        // 在最前面插入一个@|，用来去进行ansi的颜色的渲染
        builder.replace(0, 1, "@|")

        // 将括号替换为空格
        val left = builder.indexOf('(')
        builder.replace(left, left + 1, " ")

        // 将反括号替换为"|@"
        val right = builder.lastIndexOf(')')
        builder.replace(right, right + 1, "|@")
        return builder.toString()
    }
}