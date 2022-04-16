package com.wanna.framework.context

import com.wanna.framework.context.AnnotationAttributesUtils.asAnnotationAttributesSet

/**
 * 这个类主要是存放一些注解的属性，将注解中配置的相关属性全部都进行封装
 * 到这个类中，这个类继承了LinkedHashMap，key是注解的属性(方法)
 * 而value则是这个注解属性的key对应的配置的内容
 */
class AnnotationAttributes : LinkedHashMap<String?, Any?> {
    /**
     * 注解的类型
     */
    var annotationType: Class<out Annotation>? = null

    constructor() : super() {}
    constructor(annotationType: Class<out Annotation>?) : super() {
        this.annotationType = annotationType
    }

    fun getString(key: String?) = get(key) as String?


    fun getInt(key: String?): Int {
        return get(key) as Int
    }

    fun getLong(key: String?): Long {
        return get(key) as Long
    }

    fun getShort(key: String?): Short {
        return get(key) as Short
    }

    fun getBoolean(key: String?): Boolean {
        return get(key) as Boolean
    }

    fun getDouble(key: String?): Double {
        return get(key) as Double
    }

    fun getStringArray(key: String?): Array<String>? {
        return get(key) as Array<String>?
    }

    fun getClass(key: String?): Class<*>? {
        return get(key) as Class<*>?
    }

    fun getClassArray(key: String?): Array<Class<*>>? {
        return get(key) as Array<Class<*>>?
    }

    fun getObjectArray(key: String?): Array<Any>? {
        return get(key) as Array<Any>?
    }

    fun getAnnotationArray(key: String?): Array<AnnotationAttributes?> {
        return getAnnotationSet(key).toTypedArray()
    }

    fun getAnnotationSet(key: String?): Set<AnnotationAttributes?> {
        val annotations = get(key) as Array<Annotation>?
        return asAnnotationAttributesSet(*annotations!!)
    }

    fun <T> getForType(key: String?, type: Class<T>?): T? {
        return get(key) as T?
    }

    fun <T> getForTypeArray(key: String?, type: Class<T>?): Array<T>? {
        return get(key) as Array<T>?
    }
}