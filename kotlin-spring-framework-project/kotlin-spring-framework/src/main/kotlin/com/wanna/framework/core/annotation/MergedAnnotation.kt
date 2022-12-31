package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.util.Optional
import kotlin.jvm.Throws

/**
 * 一个被合成的注解
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 *
 * @param A 注解类型
 */
interface MergedAnnotation<A : Annotation> {

    /**
     * distance
     */
    val distance: Int

    /**
     * 要去进行描述的真正的注解类型
     */
    val type: Class<A>

    /**
     * 该注解是否存在于目标上? 不管是直接标注/间接标注都算
     */
    val present: Boolean

    /**
     * 该注解是否是直接标注于目标上?(如果直接标注return true; 间接标注/没有标注, return false)
     */
    val directPresent: Boolean

    /**
     * 该注解是否是间接以Meta注解的方式标注于目标上?
     */
    val metaPresent: Boolean

    /**
     * root注解, 也就是distance=0对应的注解
     */
    val root: MergedAnnotation<*>

    /**
     * 检查给定的属性名对应的注解属性是否不存在有默认值?
     *
     * @param attributeName attributeName
     * @return 如果有默认值, return false; 没有默认值return true
     */
    fun hasNonDefaultValue(attributeName: String): Boolean = !hasDefaultValue(attributeName)

    /**
     * 检查给定的属性名对应的注解属性是否有默认值?
     *
     * @param attributeName attributeName
     * @return 如果有默认值, return true; 没有默认值return false
     */
    fun hasDefaultValue(attributeName: String): Boolean

    /**
     * 获取到给定的属性名对应的字符串形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    @Throws(NoSuchElementException::class)
    fun getString(attributeName: String): String

    /**
     * 获取到给定的属性名对应的Int形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    @Throws(NoSuchElementException::class)
    fun getInt(attributeName: String): Int

    /**
     * 获取到给定的属性名对应的Long形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getLong(attributeName: String): Long

    /**
     * 获取到给定的属性名对应的Byte形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getByte(attributeName: String): Byte

    /**
     * 获取到给定的属性名对应的ByteArray形式的属性值
     *
     * @param attributeName attributeName
     * @return ByteArray属性值
     */
    fun getByteArray(attributeName: String): ByteArray

    /**
     * 获取到给定的属性名对应的Boolean形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    @Throws(NoSuchElementException::class)
    fun getBoolean(attributeName: String): Boolean

    /**
     * 获取到给定的属性名对应的Double形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    @Throws(NoSuchElementException::class)
    fun getDouble(attributeName: String): Double

    /**
     * 获取到给定的属性名对应的Float形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getFloat(attributeName: String): Float

    /**
     * 获取到给定的属性名对应的Class形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getClass(attributeName: String): Class<*>

    /**
     * 获取到给定的属性名对应的Class[]形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getClassArray(attributeName: String):Array<Class<*>>

    /**
     * 获取到给定的属性名对应的枚举形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun <E : Enum<E>> getEnum(attributeName: String, type: Class<E>): E

    /**
     * 从注解当中去获取一个可选的属性值
     *
     * @param attributeName attributeName
     * @return 属性值(如果不存在的话, return [Optional.empty])
     */
    fun getValue(attributeName: String): Optional<Any>

    /**
     * 从注解当中去获取一个可选的属性值
     *
     * @param attributeName attributeName
     * @param type 要去获取的属性值的类型
     * @return 属性值(如果不存在的话, return [Optional.empty])
     */
    fun <T : Any> getValue(attributeName: String, type: Class<T>): Optional<T>

    /**
     * 获取到经过合成之后的注解对象
     *
     * @return 合成之后的注解对象
     */
    fun synthesize(): A

    companion object {

        /**
         * 注解的Value属性名
         */
        const val VALUE: String = "value"

        @JvmStatic
        fun <A : Annotation> missing(): MergedAnnotation<A> = MissingMergedAnnotation.getInstance()

        /**
         * 快速构建起来一个MergedAnnotation的静态工厂方法
         *
         * @param classLoader ClassLoader
         * @param source source
         * @param annotationType annotationType
         * @param attributes attributes
         */
        @JvmStatic
        fun <A : Annotation> of(
            @Nullable classLoader: ClassLoader?,
            @Nullable source: Any?,
            annotationType: Class<A>,
            attributes: Map<String, Any>
        ): MergedAnnotation<A> {

            // 建立起来该AnnotationType对应的映射信息
            val mappings = AnnotationTypeMappings.forAnnotationType(annotationType)
            return TypeMappedAnnotation.of(
                mappings[0], classLoader, source, attributes, TypeMappedAnnotation.Companion::extractFromMap
            )
        }
    }
}