package com.wanna.middleware.cli.converter

import javax.annotation.Nullable

/**
 * 用于进行类型转换的Converter, 提供将字符串去转换成为别的类型的功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/27
 *
 * @param T 要去进行转换的目标类型
 */
fun interface Converter<T> {

    /**
     * 将字符串转换成为目标类型
     *
     * @param string 待转换的字符串
     * @return 将给定的字符串去转换成为目标类型的对象(可能为null)
     */
    @Nullable
    fun fromString(@Nullable string: String?): T?
}