package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.converter.Converter
import java.nio.charset.Charset

/**
 * String->Charset的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
class StringToCharsetConverter : Converter<String, Charset> {
    override fun convert(source: String?): Charset? {
        source ?: return null
        return Charset.forName(source)
    }
}