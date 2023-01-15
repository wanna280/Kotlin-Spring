package com.wanna.framework.core

import com.wanna.framework.util.ClassUtils
import java.lang.reflect.Method

/**
 * 这是一个Kotlin的探测器, 负责探测Kotlin相关的依赖是否存在; 
 * 比如探测这个类是否是一个Kotlin类, 探测是否是一个挂起函数, 探测Kotlin反射是否存在
 */
object KotlinDetector {

    /**
     * Kotlin的@Metadata注解, 标识它是一个Kotlin的类(不存在的话, 值为null)
     */
    @JvmStatic
    private var kotlinMetadata: Class<out Annotation>? = null

    /**
     * Kotlin反射是否存在的标识位？ 通过探测"kotlin.reflect.full.KClasses"去进行检查
     */
    @JvmStatic
    private var kotlinReflectPresent: Boolean =
        ClassUtils.isPresent("kotlin.reflect.full.KClasses", KotlinDetector::class.java.classLoader)

    init {
        // 尝试去寻找Kotlin的"@kotlin.Metadata"注解
        val classLoader = KotlinDetector::class.java.classLoader
        kotlinMetadata = try {
            ClassUtils.forName("kotlin.Metadata", classLoader)
        } catch (ex: ClassNotFoundException) {
            null   // 找不到就算了, pass
        }
    }

    /**
     * 检查当前环境当中Kotlin是否存在? (探测Kotlin的`@Metadata`注解是否存在)
     *
     * @return 如果当前环境当中是Kotlin环境, return true; 否则return false
     */
    @JvmStatic
    fun isKotlinPresent(): Boolean = kotlinMetadata != null

    /**
     * 它是否是一个Kotlin类型？判断Kotlin的Metadata标识类是否存在即可
     *
     * @param clazz 待探查的目标类
     * @return 如果它是KotlinType, return true; 否则return false
     */
    @JvmStatic
    fun isKotlinType(clazz: Class<*>): Boolean = kotlinMetadata != null && clazz.isAnnotationPresent(kotlinMetadata!!)

    /**
     * 是否Kotlin反射的相关依赖存在？
     *
     * @return Kotlin反射存在, return true; 否则return false
     */
    @JvmStatic
    fun isKotlinReflectPresent(): Boolean = kotlinReflectPresent

    /**
     * 是否是一个Kotlin的挂起函数？只需要判断函数的最后一个参数是否是Continuation即可
     *
     * @param method 要去检测的方法
     * @return 它是否是一个Kotlin的挂起函数? (如果最后一个参数是Continuation, return true; 否则return false)
     */
    @JvmStatic
    fun isSuspendingFunction(method: Method): Boolean {
        if (isKotlinType(method.declaringClass)) {
            val parameterTypes = method.parameterTypes
            return parameterTypes.isNotEmpty() && parameterTypes[parameterTypes.size - 1].name == "kotlin.coroutines.Continuation"
        }
        return false
    }
}