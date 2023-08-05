package com.wanna.framework.beans

import com.wanna.framework.lang.Nullable
import kotlin.jvm.Throws

/**
 * TypeConverter, 提供类型的转换工作
 */
interface TypeConverter {
    /**
     * 如果必要的话, 去进行类型的转换
     *
     * @param value 要去进行转换的值
     * @param requiredType 要将value转换成为什么类型?
     * @return 经过转换之后的属性值
     */
    @Throws(TypeMismatchException::class)
    @Nullable
    fun <T : Any> convertIfNecessary(@Nullable value: Any?, @Nullable requiredType: Class<T>?): T?
}