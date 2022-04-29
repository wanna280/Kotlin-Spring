package com.wanna.framework.core.convert.converter

/**
 * 这是一个支持泛型的Converter
 */
interface GenericConverter {

    /**
     * 获取可以转换的类型列表
     */
    fun getConvertibleTypes(): Set<ConvertiblePair>?

    fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T?

    /**
     * 这是一个可以转换的类型的Pair对，
     */
    class ConvertiblePair() {
        var sourceType: Class<*>? = null

        var targetType: Class<*>? = null

        constructor(sourceType: Class<*>, targetType: Class<*>) : this() {
            this.sourceType = sourceType
            this.targetType = targetType
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ConvertiblePair

            if (sourceType != other.sourceType) return false
            if (targetType != other.targetType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = sourceType?.hashCode() ?: 0
            result = 31 * result + (targetType?.hashCode() ?: 0)
            return result
        }


    }
}