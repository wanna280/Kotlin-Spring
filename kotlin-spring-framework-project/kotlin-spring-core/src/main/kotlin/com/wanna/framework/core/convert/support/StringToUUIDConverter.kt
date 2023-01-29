package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.converter.Converter
import java.util.*

/**
 * String->UUID的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
class StringToUUIDConverter : Converter<String, UUID> {
    override fun convert(source: String?): UUID? {
        source ?: return null
        return UUID.fromString(source)
    }
}