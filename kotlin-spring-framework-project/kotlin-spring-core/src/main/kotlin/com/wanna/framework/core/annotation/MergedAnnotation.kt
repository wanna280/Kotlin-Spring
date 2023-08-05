package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.util.EnumSet
import java.util.Optional
import java.util.function.Function
import java.util.function.Predicate
import kotlin.jvm.Throws

/**
 * 一个被Merged之后得到的注解
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
     * 对MergedAnnotation当中的属性值去进行过滤, 得到一个新的MergedAnnotation
     *
     * @param predicate 对属性名去执行匹配的断言, 如果断言和属性名匹配了, 那么该属性将会被pass掉
     * @return 只用于符合断言的属性名的MergedAnnotation
     */
    fun filterAttributes(predicate: Predicate<String>): MergedAnnotation<A>

    /**
     * 获取到不使用Merged的原始注解的属性的MergedAnnotation(对于@AliasFor将不会生效)
     *
     * @return 不含有Merged属性的MergedAnnotation
     */
    fun withNonMergedAttributes(): MergedAnnotation<A>

    /**
     * 获取到给定的属性名对应的字符串形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    @Throws(NoSuchElementException::class)
    fun getString(attributeName: String): String

    /**
     * 获取到给定的属性名对应的String[]形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getStringArray(attributeName: String): Array<String>

    /**
     * 获取到给定的属性名对应的Int形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    @Throws(NoSuchElementException::class)
    fun getInt(attributeName: String): Int

    /**
     * 获取到给定的属性名对应的Int[]形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getIntArray(attributeName: String): IntArray

    /**
     * 获取到给定的属性名对应的Long形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getLong(attributeName: String): Long

    /**
     * 获取到给定的属性名对应的Long[]形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getLongArray(attributeName: String): LongArray

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
     * 获取到给定的属性名对应的Boolean[]形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    @Throws(NoSuchElementException::class)
    fun getBooleanArray(attributeName: String): BooleanArray

    /**
     * 获取到给定的属性名对应的Double形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    @Throws(NoSuchElementException::class)
    fun getDouble(attributeName: String): Double

    /**
     * 获取到给定的属性名对应的Double形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    @Throws(NoSuchElementException::class)
    fun getDoubleArray(attributeName: String): DoubleArray

    /**
     * 获取到给定的属性名对应的Float形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getFloat(attributeName: String): Float

    /**
     * 获取到给定的属性名对应的Float形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun getFloatArray(attributeName: String): FloatArray

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
    fun getClassArray(attributeName: String): Array<Class<*>>

    /**
     * 获取到给定的属性名对应的枚举形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun <E : Enum<E>> getEnum(attributeName: String, type: Class<E>): E

    /**
     * 获取到给定的属性名对应的Enum[]的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun <E : Enum<E>> getEnumArray(attributeName: String, type: Class<Array<E>>): Array<E>

    /**
     * 获取到给定的属性名对应的Annotation[]形式的属性值
     *
     * @param attributeName attributeName
     * @return 属性值
     */
    fun <A : Annotation> getAnnotationArray(attributeName: String, type: Class<Array<A>>): Array<A>

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
     * 从MergedAnnotation当中获取给定的属性名的默认值
     *
     * @param attributeName attributeName
     * @return 该属性名对应的默认值的Optional, 如果不存在的话, return [Optional.empty]
     */
    fun getDefaultValue(attributeName: String): Optional<Any>

    /**
     * 从MergedAnnotation当中获取给定的属性名的默认值
     *
     * @param attributeName attributeName
     * @param type 想要获取的默认值的类型
     * @return 该属性名对应的默认值的Optional, 如果不存在的话, return [Optional.empty]
     */
    fun <T : Any> getDefaultValue(attributeName: String, type: Class<T>): Optional<T>

    /**
     * 获取到经过合成之后的注解对象
     *
     * @return 合成之后的注解对象
     */
    fun synthesize(): A

    /**
     * 获取到经过合成的代理对象
     *
     * @param condition 当前注解要符合的要求的断言
     * @return 断言和当前MergedAnnotation匹配的话, return合成之后的注解; 否则return [Optional.empty]
     */
    fun synthesize(condition: Predicate<in MergedAnnotation<A>>): Optional<A>

    /**
     * 将当前的MergedAnnotation去转换成为AnnotationAttributes(mutable, 可变)
     *
     * @param adapts 转换时需要用到的操作(是否需要将Class转String/是否需要将Annotation转Map)
     * @return 转换之后得到的AnnotationAttributes
     */
    fun asAnnotationAttributes(vararg adapts: Adapt): AnnotationAttributes

    /**
     * 将当前的MergedAnnotation去转换成为Map(immutable, 不可变)
     *
     * @param adapts 转换时需要用到的操作(是否需要将Class转String/是否需要将Annotation转Map)
     * @return 转换之后得到的Map
     */
    fun asMap(vararg adapts: Adapt): Map<String, Any>

    /**
     * 将当前的MergedAnnotation去转换成为Map
     *
     * @param factory 将MergedAnnotation转换为Map的Factory
     * @param adapts 转换时需要用到的操作(是否需要将Class转String/是否需要将Annotation转Map)
     * @param T 希望得到的目标Map的类型
     * @return 转换之后得到的Map
     */
    fun <T : Map<String, Any>> asMap(factory: Function<MergedAnnotation<A>, T>, vararg adapts: Adapt): T

    /**
     * 当为一个注解的属性值去创建Map/AnnotationAttributes时, 需要执行的操作
     */
    enum class Adapt {
        /**
         * 是否需要去将一个Class的属性值去转换为字符串?
         */
        CLASS_TO_STRING,

        /**
         * 是否需要将一个注解的属性值去转换成为一个Map, 而不是使用合成注解?
         */
        ANNOTATION_TO_MAP;

        /**
         * 检查当前的Adapt, 是否在给定的adapts列表当中
         *
         * @param adapts 待检查的Adapt列表
         * @return 如果this在adapts当中return true; 否则return false
         */
        fun isIn(vararg adapts: Adapt): Boolean {
            for (adapt in adapts) {
                if (this === adapt) {
                    return true
                }
            }
            return false
        }

        companion object {
            /**
             * 根据classToString和annotationsToMap的标志位, 创建出来对应的Adapt枚举值列表
             *
             * @param classToString 是否需要将Class去转换成为字符串
             * @param annotationsToMap 是否需要将注解去转换成为Map
             * @return 需要的Adapt对象列表
             */
            @JvmStatic
            fun values(classToString: Boolean, annotationsToMap: Boolean): Array<Adapt> {
                val enumSet = EnumSet.noneOf(Adapt::class.java)
                addIfTrue(enumSet, CLASS_TO_STRING, classToString)
                addIfTrue(enumSet, ANNOTATION_TO_MAP, annotationsToMap)
                return enumSet.toArray(emptyArray<Adapt>())
            }

            /**
             * 如果test条件满足的话, 将value添加到result当中
             *
             * @param result result
             * @param value 要去添加的元素类型
             * @param test 条件(为true时才添加)
             */
            @JvmStatic
            private fun <E> addIfTrue(result: MutableSet<E>, value: E, test: Boolean) {
                if (test) {
                    result += value
                }
            }
        }

    }

    companion object {

        /**
         * 注解的Value属性名
         */
        const val VALUE: String = "value"

        /**
         * 没有标注这个注解的MergedAnnotation的标识常量
         *
         * @return missing
         */
        @JvmStatic
        fun <A : Annotation> missing(): MergedAnnotation<A> = MissingMergedAnnotation.getInstance()

        /**
         * 根据一个给定的注解实例, 去构建出来MergedAnnotation
         *
         * @param annotation Annotation
         * @return MergedAnnotation for given Annotation
         */
        @JvmStatic
        fun <A : Annotation> from(annotation: A): MergedAnnotation<A> {
            return from(null, annotation)
        }

        /**
         * 根据一个给定的注解实例, 去构建出来MergedAnnotation
         *
         * @param source source
         * @param annotation Annotation
         * @return MergedAnnotation for given Annotation
         */
        @JvmStatic
        fun <A : Annotation> from(@Nullable source: Any?, annotation: A): MergedAnnotation<A> {
            return TypeMappedAnnotation.from(source, annotation)
        }

        /**
         * 根据注解的Attributes去快速构建起来一个MergedAnnotation的静态工厂方法
         *
         * @param annotationType annotationType
         * @param attributes attributes
         * @return MergedAnnotation for given Annotation
         */
        @JvmStatic
        fun <A : Annotation> of(
            annotationType: Class<A>, attributes: Map<String, Any>
        ): MergedAnnotation<A> = of(null, annotationType, attributes)

        /**
         * 根据注解的Attributes去快速构建起来一个MergedAnnotation的静态工厂方法
         *
         * @param source source
         * @param annotationType annotationType
         * @param attributes attributes
         * @return MergedAnnotation for given Annotation
         */
        @JvmStatic
        fun <A : Annotation> of(
            @Nullable source: Any?, annotationType: Class<A>, attributes: Map<String, Any>
        ): MergedAnnotation<A> = of(null, source, annotationType, attributes)

        /**
         * 根据注解的Attributes去快速构建起来一个MergedAnnotation的静态工厂方法
         *
         * @param classLoader ClassLoader
         * @param source source
         * @param annotationType annotationType
         * @param attributes attributes
         * @return MergedAnnotation for given Annotation
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