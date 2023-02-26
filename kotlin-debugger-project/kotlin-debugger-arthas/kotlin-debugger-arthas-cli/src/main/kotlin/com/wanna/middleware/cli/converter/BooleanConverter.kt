package com.wanna.middleware.cli.converter

import javax.annotation.Nullable

/**
 * Boolean的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/27
 */
object BooleanConverter : Converter<Boolean> {

    /**
     * 值为true的一些可选项
     */
    @JvmStatic
    private val TRUE = listOf("true", "yes", "on", "1")

    override fun fromString(@Nullable string: String?): Boolean {
        return string != null && TRUE.contains(string.toString())
    }
}