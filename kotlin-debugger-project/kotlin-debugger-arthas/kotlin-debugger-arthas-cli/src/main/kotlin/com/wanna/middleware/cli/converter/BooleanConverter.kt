package com.wanna.middleware.cli.converter

import javax.annotation.Nullable

/**
 * Boolean的Converter, 用于去将字符串转换成为Boolean
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/27
 */
object BooleanConverter : Converter<Boolean> {

    /**
     * 值为true的一些可选项, 对于"true"/"yes"/"on"/"1"这些字符串, 我们都认为为true
     */
    @JvmStatic
    private val TRUE = listOf("true", "yes", "on", "1")

    /**
     * 根据给定的字符串, 将它去转换成为Boolean
     *
     * @param string 待转换的字符串
     * @return 如果该字符串为true/yes/on/1, 那么return true; 否则return false
     */
    override fun fromString(@Nullable string: String?): Boolean {
        return string != null && TRUE.contains(string.toString())
    }
}