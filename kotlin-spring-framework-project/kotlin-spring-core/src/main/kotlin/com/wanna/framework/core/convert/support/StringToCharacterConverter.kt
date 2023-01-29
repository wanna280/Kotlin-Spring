package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.converter.Converter
import com.wanna.framework.util.StringUtils

/**
 * String->Char的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
class StringToCharacterConverter : Converter<String, Char> {
    override fun convert(source: String?): Char? {
        if (!StringUtils.hasText(source)) {
            return null
        }
        if (source!!.length != 1) {
            throw IllegalArgumentException("[String]->[Character]类型转换时, 原始的字符串[$source]的长度不为1")
        }
        return source[0]
    }
}