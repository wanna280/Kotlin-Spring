package com.wanna.middleware.cli.converter

/**
 * String的Converter, 无需执行任何操作, 直接返回原始的对象即可
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