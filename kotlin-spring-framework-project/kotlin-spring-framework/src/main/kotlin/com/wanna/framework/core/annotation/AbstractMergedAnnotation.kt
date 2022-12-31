package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.util.*
import kotlin.NoSuchElementException
import kotlin.jvm.Throws

/**
 * 抽象的MergedAnnotation的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 *
 * @see MergedAnnotation
 */
abstract class AbstractMergedAnnotation<A : Annotation> : MergedAnnotation<A> {

    /**
     * 合成注解对象
     *
     * @see SynthesizedMergedAnnotationInvocationHandler
     */
    @Nullable
    @Volatile
    private var synthesizedAnnotation: A? = null

    /**
     * 该注解是否是直接标注于目标上?
     *
     * @return 如果直接标注return true; 间接标注/没有标注, return false
     */
    override val directPresent: Boolean
        get() = present && distance == 0

    /**
     * 该注解是否是间接以Meta注解的方式标注于目标上?
     *
     * @return 如果直接标注return true; 间接标注/没有标注, return false
     */
    override val metaPresent: Boolean
        get() = present && distance > 0

    /**
     * 执行注解的合成
     *
     * @return 合成之后的注解对象
     */
    override fun synthesize(): A {
        var annotation = this.synthesizedAnnotation
        if (annotation == null) {
            annotation = createSynthesized()
            this.synthesizedAnnotation = annotation
        }
        return annotation
    }

    override fun getString(attributeName: String): String = getRequiredAttributeValue(attributeName, String::class.java)

    override fun getInt(attributeName: String) = getRequiredAttributeValue(attributeName, Int::class.javaObjectType)

    override fun getBoolean(attributeName: String) =
        getRequiredAttributeValue(attributeName, Boolean::class.javaObjectType)

    override fun getLong(attributeName: String) = getRequiredAttributeValue(attributeName, Long::class.javaObjectType)

    override fun getByte(attributeName: String) = getRequiredAttributeValue(attributeName, Byte::class.javaObjectType)

    override fun getByteArray(attributeName: String) = getRequiredAttributeValue(attributeName, ByteArray::class.java)

    override fun getDouble(attributeName: String) =
        getRequiredAttributeValue(attributeName, Double::class.javaObjectType)

    override fun getFloat(attributeName: String) = getRequiredAttributeValue(attributeName, Float::class.javaObjectType)

    override fun getClass(attributeName: String) = getRequiredAttributeValue(attributeName, Class::class.java)

    override fun <E : Enum<E>> getEnum(attributeName: String, type: Class<E>): E =
        getRequiredAttributeValue(attributeName, type)

    /**
     * 获取到给定类型的属性值
     *
     * @param attributeName attributeName
     * @param type 属性值类型
     * @param T 属性值类型
     * @return 从注解当中根据AttributeName去获取到的属性值
     * @throws NoSuchElementException 如果注解当中不存在有这样的属性的话
     */
    @Throws(NoSuchElementException::class)
    private fun <T> getRequiredAttributeValue(attributeName: String, type: Class<T>): T {
        return getAttributeValue(attributeName, type)
            ?: throw NoSuchElementException("No attribute named $attributeName in ${type.name}")
    }

    /**
     * 从注解当中去获取一个可选的属性值
     *
     * @param attributeName attributeName
     * @return 属性值(如果不存在的话, return [Optional.empty])
     */
    override fun getValue(attributeName: String): Optional<Any> = getValue(attributeName, Any::class.java)

    /**
     * 从注解当中去获取一个可选的属性值
     *
     * @param attributeName attributeName
     * @param type 要去获取的属性值的类型
     * @return 属性值(如果不存在的话, return [Optional.empty])
     */
    override fun <T : Any> getValue(attributeName: String, type: Class<T>) =
        Optional.ofNullable(getAttributeValue(attributeName, type))

    /**
     * 从注解当中去获取到属性值, 模板方法, 交给子类去完成
     *
     * @param attributeName attributeName
     * @param type type
     * @return 属性值, 获取不到return null
     * @throws IllegalArgumentException 如果该属性名对应的属性值存在, 但是类型不匹配的话
     * @throws NoSuchElementException 如果该属性值是必须要的, 但是却没有找到
     */
    @Nullable
    protected abstract fun <T> getAttributeValue(attributeName: String, type: Class<T>): T?

    /**
     * 模板方法, 交给子类去进行真正的注解的合成
     *
     * @return 合成之后的注解对象
     */
    protected abstract fun createSynthesized(): A
}