package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.converter.Converter

/**
 * 将对象去转换成为字符串的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
class ObjectToStringConverter : Converter<Any, String> {
    override fun convert(source: Any?): String = source.toString()
}