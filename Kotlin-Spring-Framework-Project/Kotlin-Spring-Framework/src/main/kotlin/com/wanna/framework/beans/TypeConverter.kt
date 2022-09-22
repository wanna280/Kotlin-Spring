package com.wanna.framework.beans

import com.wanna.framework.lang.Nullable

/**
 * TypeConverter，提供类型的转换工作
 */
interface TypeConverter {
    /**
     * 如果必要的话，去进行类型的转换
     *
     * @param value 要去进行转换的值
     * @param requiredType 要将value转换成为什么类型？
     */
    @Nullable
    fun <T> convertIfNecessary(value: Any?, requiredType: Class<T>?): T?
}