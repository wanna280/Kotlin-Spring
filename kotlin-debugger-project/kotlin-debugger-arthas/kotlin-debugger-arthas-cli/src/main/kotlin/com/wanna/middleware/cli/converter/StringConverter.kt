package com.wanna.middleware.cli.converter

/**
 * String的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/27
 */
object StringConverter : Converter<String> {

    override fun fromString(string: String?): String? {
        return string
    }
}