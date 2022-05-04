package com.wanna.framework.core

import com.wanna.framework.core.util.ClassUtils
import java.lang.reflect.Method

/**
 * 这是一个Kotlin的探测器，负责探测Kotlin相关的依赖是否存在
 */
object KotlinDetector {

    private var kotlinMetata: Class<out Annotation>? = null  // Kotlin Metadata

    private var kotlinReflectPresent: Boolean = false // Kotlin反射是否存在？

    init {
        val classLoader = KotlinDetector::class.java.classLoader
        kotlinMetata = try {
            ClassUtils.forName("kotlin.Metadata", classLoader)
        } catch (ex: ClassNotFoundException) {
            null
        }
        kotlinReflectPresent = ClassUtils.isPresent("kotlin.reflect.full.KClasses", classLoader)
    }

    /**
     * Kotlin是否存在？
     */
    fun isKotlinPresent(): Boolean = kotlinMetata != null

    /**
     * 它是否是一个Kotlin类型？判断Kotlin的Metadata标识类是否存在即可
     */
    fun isKotlinType(clazz: Class<*>): Boolean = kotlinMetata != null && clazz.isAnnotationPresent(kotlinMetata!!)

    /**
     * 是否Kotlin反射存在？
     */
    fun isKotlinReflectPresent(): Boolean = kotlinReflectPresent

    /**
     * 是否是一个Kotlin的挂起函数？只需要判断函数的最后一个参数是否是Continuation即可
     */
    fun isSuspendingFunction(method: Method): Boolean {
        if (isKotlinType(method.declaringClass)) {
            val parameterTypes = method.parameterTypes
            return parameterTypes.isNotEmpty() && parameterTypes[parameterTypes.size - 1].name == "kotlin.coroutines.Continuation"
        }
        return false
    }
}