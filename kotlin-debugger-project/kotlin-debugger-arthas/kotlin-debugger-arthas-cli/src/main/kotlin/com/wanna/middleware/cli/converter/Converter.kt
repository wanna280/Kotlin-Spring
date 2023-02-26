package com.wanna.middleware.cli.converter

import javax.annotation.Nullable

/**
 * 类型转换的Converter, 提供将字符串去转换成为别的类型
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/27
 */
fun interface Converter<T> {

    @Nullable
    fun fromString(@Nullable string: String?): T?
}