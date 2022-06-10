package com.wanna.framework.core.convert.converter

/**
 * 这是一个支持泛型的Converter
 */
interface GenericConverter {

    /**
     * 获取可以转换的类型映射列表
     */
    fun getConvertibleTypes(): Set<ConvertiblePair>?

    /**
     * 将source对象从sourceType转换到targetType
     *
     * @param source 要进行转换的对象
     * @param sourceType 源类型
     * @param targetType 目标类型
     */
    fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T?

    /**
     * 这是一个可以转换的类型的Pair对，维护了sourceType和targetType；
     * 需要去重写equals方法和hashCode方法，保证该类型可以作为Key去参与hash计算，并获取到对应的value
     *
     * @param sourceType sourceType
     * @param targetType targetType
     */
    class ConvertiblePair(val sourceType: Class<*>, val targetType: Class<*>) {
        override fun hashCode(): Int = 31 * sourceType.hashCode() + targetType.hashCode()
        override fun toString() = "${sourceType.name} --> ${targetType.name}"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ConvertiblePair
            return sourceType == other.sourceType && targetType == other.targetType
        }
    }
}