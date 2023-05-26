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

    /**
     * 将给定的字符串去转换成为给定的字符
     *
     * @param string 待转换的字符串
     * @throws IllegalStateException 如果给定的字符串为null, 或者是字符串长度不为1
     */
    override fun fromString(@Nullable string: String?): Char {
        string ?: throw IllegalStateException("Input string must be non null")
        if (string.length != 1) {
            throw IllegalStateException("The input string $string cannot convert to a character, The input's length must be 1")
        }
        return string[0]
    }
}