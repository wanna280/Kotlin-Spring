package com.wanna.middleware.cli.converter

import javax.annotation.Nullable

/**
 * 将字符串去转换成为Char字符的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/27
 */
object CharacterConverter : Converter<Char> {
    override fun fromString(@Nullable string: String?): Char {
        string ?: throw IllegalStateException("Input string must be non null")
        if (string.length != 1) {
            throw IllegalStateException("The input string $string cannot convert to a character, The input's length must be 1")
        }
        return string[0]
    }
}