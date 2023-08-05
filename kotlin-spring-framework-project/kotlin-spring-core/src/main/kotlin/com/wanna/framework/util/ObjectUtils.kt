package com.wanna.framework.util

import com.wanna.framework.lang.Nullable
import java.util.*

/**
 * 提供对于Object的相关工具类, 比如toString, equals相关的操作的工具方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/9
 */
object ObjectUtils {

    /**
     * null的字符串
     */
    private const val NULL_STRING = "null"

    /**
     * 数组toString的前缀
     */
    private const val ARRAY_START = "{"

    /**
     * 数组toString的后缀
     */
    private const val ARRAY_END = "}"

    /**
     * 空数组的toString常量
     */
    private const val EMPTY_ARRAY = "$ARRAY_START$ARRAY_END"

    /**
     * 数组当中的元素的分隔符
     */
    private const val ARRAY_ELEMENT_SEPARATOR = ", "


    /**
     * 检查给定的两个对象, 是否equals? 使用的空安全的比较方式
     *
     * * 1.对于普通对象, 基于equals去进行比较
     * * 2.对于数组对象, 可以去对数组当中的每个元素去进行比较
     *
     * @param o1 o1
     * @param o2 o2
     */
    @JvmStatic
    fun nullSafeEquals(@Nullable o1: Any?, @Nullable o2: Any?): Boolean {
        if (o1 === o2) {
            return true
        }
        // 如果其中一个为null, 另外一个不为null
        if (o1 === null || o2 === null) {
            return false
        }
        if (o1 == o2) {
            return true
        }
        // 如果两个都是数组的话, 那么去比较一下数组当中的每个元素
        if (o1.javaClass.isArray && o2.javaClass.isArray) {
            return arrayEquals(o1, o2)
        }
        return false
    }

    /**
     * 比较两个数组对象当中的元素是否相同?
     *
     * @param o1 o1
     * @param o2
     * @return 如果两个数组当中元素完全相同, return true; 否则return false
     */
    @JvmStatic
    private fun arrayEquals(@Nullable o1: Any?, @Nullable o2: Any?): Boolean {
        if (o1 is Array<*> && o2 is Array<*>) {
            return o1.contentEquals(o2)
        }
        if (o1 is ByteArray && o2 is ByteArray) {
            return o1.contentEquals(o2)
        }
        if (o1 is ShortArray && o2 is ShortArray) {
            return o1.contentEquals(o2)
        }
        if (o1 is IntArray && o2 is IntArray) {
            return o1.contentEquals(o2)
        }
        if (o1 is LongArray && o2 is LongArray) {
            return o1.contentEquals(o2)
        }
        if (o1 is BooleanArray && o2 is BooleanArray) {
            return o1.contentEquals(o2)
        }
        if (o1 is CharArray && o2 is CharArray) {
            return o1.contentEquals(o2)
        }
        if (o1 is DoubleArray && o2 is DoubleArray) {
            return o1.contentEquals(o2)
        }
        if (o1 is FloatArray && o2 is FloatArray) {
            return o1.contentEquals(o2)
        }
        return false
    }

    /**
     * 空安全的对象toString
     *
     * * 1.如果给定的对象为null, 返回"null";
     * * 2.如果给定的对象是数组, 那么使用数组的方式去进行toString;
     * * 3.如果给定的对象不是数组的话, 那么直接toString去进行生成
     *
     * @param obj 待toString的对象
     * @return toString之后得到的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable obj: Any?): String {
        if (obj === null) {
            return NULL_STRING
        }
        if (obj is Array<*>) {
            return nullSafeToString(obj)
        }
        if (obj is ByteArray) {
            return nullSafeToString(obj)
        }
        if (obj is ShortArray) {
            return nullSafeToString(obj)
        }
        if (obj is IntArray) {
            return nullSafeToString(obj)
        }
        if (obj is LongArray) {
            return nullSafeToString(obj)
        }
        if (obj is FloatArray) {
            return nullSafeToString(obj)
        }
        if (obj is DoubleArray) {
            return nullSafeToString(obj)
        }
        if (obj is BooleanArray) {
            return nullSafeToString(obj)
        }
        if (obj is CharArray) {
            return nullSafeToString(obj)
        }
        return obj.toString()
    }

    /**
     * 空安全的数组toString
     *
     * @param array 待toString的数组
     * @return 针对该数组去toString的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable array: Array<*>?): String {
        array ?: return NULL_STRING
        if (array.isEmpty()) {
            return EMPTY_ARRAY
        }
        val joiner = StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END)
        array.forEach { joiner.add(it.toString()) }
        return joiner.toString()
    }

    /**
     * 空安全的Byte数组toString
     *
     * @param array 待toString的数组
     * @return 针对该数组去toString的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable array: ByteArray?): String {
        array ?: return NULL_STRING
        if (array.isEmpty()) {
            return EMPTY_ARRAY
        }
        val joiner = StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END)
        array.forEach { joiner.add(it.toString()) }
        return joiner.toString()
    }

    /**
     * 空安全的Short数组toString
     *
     * @param array 待toString的数组
     * @return 针对该数组去toString的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable array: ShortArray?): String {
        array ?: return NULL_STRING
        if (array.isEmpty()) {
            return EMPTY_ARRAY
        }
        val joiner = StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END)
        array.forEach { joiner.add(it.toString()) }
        return joiner.toString()
    }

    /**
     * 空安全的Int数组toString
     *
     * @param array 待toString的数组
     * @return 针对该数组去toString的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable array: IntArray?): String {
        array ?: return NULL_STRING
        if (array.isEmpty()) {
            return EMPTY_ARRAY
        }
        val joiner = StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END)
        array.forEach { joiner.add(it.toString()) }
        return joiner.toString()
    }

    /**
     * 空安全的Long数组toString
     *
     * @param array 待toString的数组
     * @return 针对该数组去toString的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable array: LongArray?): String {
        array ?: return NULL_STRING
        if (array.isEmpty()) {
            return EMPTY_ARRAY
        }
        val joiner = StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END)
        array.forEach { joiner.add(it.toString()) }
        return joiner.toString()
    }

    /**
     * 空安全的Double数组toString
     *
     * @param array 待toString的数组
     * @return 针对该数组去toString的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable array: DoubleArray?): String {
        array ?: return NULL_STRING
        if (array.isEmpty()) {
            return EMPTY_ARRAY
        }
        val joiner = StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END)
        array.forEach { joiner.add(it.toString()) }
        return joiner.toString()
    }

    /**
     * 空安全的Float数组toString
     *
     * @param array 待toString的数组
     * @return 针对该数组去toString的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable array: FloatArray?): String {
        array ?: return NULL_STRING
        if (array.isEmpty()) {
            return EMPTY_ARRAY
        }
        val joiner = StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END)
        array.forEach { joiner.add(it.toString()) }
        return joiner.toString()
    }

    /**
     * 空安全的Boolean数组toString
     *
     * @param array 待toString的数组
     * @return 针对该数组去toString的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable array: BooleanArray?): String {
        array ?: return NULL_STRING
        if (array.isEmpty()) {
            return EMPTY_ARRAY
        }
        val joiner = StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END)
        array.forEach { joiner.add(it.toString()) }
        return joiner.toString()
    }

    /**
     * 空安全的Char数组toString
     *
     * @param array 待toString的数组
     * @return 针对该数组去toString的结果
     */
    @JvmStatic
    fun nullSafeToString(@Nullable array: CharArray?): String {
        array ?: return NULL_STRING
        if (array.isEmpty()) {
            return EMPTY_ARRAY
        }
        val joiner = StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END)
        array.forEach { joiner.add("\'" + it.toString() + "\'") }
        return joiner.toString()
    }

}