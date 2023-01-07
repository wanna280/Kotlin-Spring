package com.wanna.framework.context.annotation

import com.wanna.framework.constants.CLASS_ARRAY_TYPE
import com.wanna.framework.constants.STRING_ARRAY_TYPE
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils

/**
 * 这个类主要是存放一些注解的属性, 将注解中配置的相关属性全部都进行封装到这个类中,
 * 这个类继承了LinkedHashMap, attributeName是注解的属性(方法), 而value则是这个注解属性的attributeName对应的配置的内容
 *
 * @param annotationType 注解类型
 */
@Suppress("UNCHECKED_CAST")
open class AnnotationAttributes(@Nullable val annotationType: Class<out Annotation>?) : LinkedHashMap<String, Any>() {

    /**
     * 提供一个基于具体的注解类型的类名&ClassLoader的构造器
     *
     * @param annotationName 注解的类名
     * @param classLoader 提供AnnotationName的类加载的ClassLoader
     */
    constructor(annotationName: String, classLoader: ClassLoader) : this(
        ClassUtils.forName(
            annotationName,
            classLoader
        )
    )

    /**
     * 提供一个无参数构造器
     */
    constructor() : this(null)

    open fun getString(attributeName: String) = getRequiredAttribute(attributeName, String::class.java)

    open fun getInt(attributeName: String): Int = getRequiredAttribute(attributeName, Int::class.java)

    open fun getLong(attributeName: String): Long = getRequiredAttribute(attributeName, Long::class.java)
    open fun getShort(attributeName: String): Short = getRequiredAttribute(attributeName, Short::class.java)

    open fun getBoolean(attributeName: String): Boolean = getRequiredAttribute(attributeName, Boolean::class.java)

    open fun getDouble(attributeName: String): Double = getRequiredAttribute(attributeName, Double::class.java)

    open fun getClass(attributeName: String): Class<*> = getRequiredAttribute(attributeName, Class::class.java)

    open fun getStringArray(attributeName: String): Array<String> =
        getRequiredAttribute(attributeName, STRING_ARRAY_TYPE)

    open fun getClassArray(attributeName: String): Array<Class<*>> {
        return getRequiredAttribute(attributeName, CLASS_ARRAY_TYPE)
    }

    open fun getObjectArray(attributeName: String): Array<Any> =
        getRequiredAttribute(attributeName, Array<Any>::class.java)

    open fun getAnnotationArray(attributeName: String): Array<Annotation> =
        getRequiredAttribute(attributeName, Array<Annotation>::class.java)

    open fun <T> getForType(attributeName: String, type: Class<T>): T = getRequiredAttribute(attributeName, type)

    private fun <T> getRequiredAttribute(attributeName: String, expectedType: Class<T>): T {
        val attributeValue = get(attributeName)
        if (!ClassUtils.isAssignableValue(expectedType, attributeValue)) {
            throw IllegalStateException("type mismatch, attributeName=$attributeName, expectdType=$expectedType")
        }
        return attributeValue as T
    }

    companion object {
        /**
         * 从一个Map转换到Attributes对象
         *
         * @param map map
         * @return AnnotationAttributes对象(如果map为null, return null)
         */
        @Nullable
        @JvmStatic
        fun fromMap(@Nullable map: Map<String, Any>?): AnnotationAttributes? {
            map ?: return null
            if (map is AnnotationAttributes) {
                return map
            }
            val attributes = AnnotationAttributes()
            attributes.putAll(map)
            return attributes
        }
    }
}