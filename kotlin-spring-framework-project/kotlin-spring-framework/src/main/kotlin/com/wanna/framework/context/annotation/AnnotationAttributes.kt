package com.wanna.framework.context.annotation

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils

/**
 * 这个类主要是存放一些注解的属性, 将注解中配置的相关属性全部都进行封装到这个类中,
 * 这个类继承了LinkedHashMap, key是注解的属性(方法), 而value则是这个注解属性的key对应的配置的内容
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

    open fun getString(key: String) = get(key) as String

    open fun getInt(key: String): Int {
        return get(key) as Int
    }

    open fun getLong(key: String): Long {
        return get(key) as Long
    }

    open fun getShort(key: String): Short {
        return get(key) as Short
    }

    open fun getBoolean(key: String): Boolean {
        return get(key) as Boolean
    }

    open fun getDouble(key: String): Double {
        return get(key) as Double
    }

    open fun getStringArray(key: String): Array<String> {
        return get(key) as Array<String>
    }

    open fun getClass(key: String): Class<*> {
        return get(key) as Class<*>
    }

    open fun getClassArray(key: String): Array<Class<*>> {
        return get(key) as Array<Class<*>>
    }

    open fun getObjectArray(key: String): Array<Any> {
        return get(key) as Array<Any>
    }

    open fun getAnnotationArray(key: String): Array<Annotation> {
        return get(key) as Array<Annotation>
    }

    open fun getAnnotationSet(key: String): Set<Annotation> {
        return getAnnotationArray(key).toSet()
    }

    open fun <T> getForType(key: String, type: Class<T>): T {
        return get(key) as T
    }

    open fun <T> getForTypeArray(key: String, type: Class<T>): Array<T> {
        return get(key) as Array<T>
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