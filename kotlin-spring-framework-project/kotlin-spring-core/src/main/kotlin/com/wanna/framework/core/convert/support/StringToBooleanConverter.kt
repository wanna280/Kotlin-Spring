package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.converter.Converter
import com.wanna.framework.util.StringUtils

/**
 * String->Boolean的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
class StringToBooleanConverter : Converter<String, Boolean> {
    companion object {
        @JvmStatic
        private val trueValues = setOf("true", "on", "1", "yes")

        @JvmStatic
        private val falseValues = setOf("false", "off", "0", "no")
    }

    override fun convert(source: String?): Boolean? {
        if (!StringUtils.hasText(source)) {
            return null
        }
        val value = source!!.lowercase()
        if (trueValues.contains(value)) {
            return true
        } else if (falseValues.contains(value)) {
            return false
        } else {
            throw IllegalArgumentException("不合法的Boolean值[$source]")
        }
    }
}