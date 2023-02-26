package com.wanna.middleware.cli.impl

import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

/**
 * 反射工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/27
 */
object ReflectionUtils {

    /**
     * 检查给定的Setter的参数是否是一个Multiple? 也就是是否是一个集合/数组
     *
     * @param setter setter
     * @return 如果该参数是集合/数组的话, 那么return true; 否则return false
     */
    @JvmStatic
    fun isMultiple(setter: Method): Boolean {
        if (setter.parameterCount != 1) {
            throw IllegalStateException("Setter parameter count should be 1")
        }
        val type = setter.parameterTypes[0]
        return type.isArray && Collection::class.java.isAssignableFrom(type)
    }

    /**
     * 获取给定的方法的对应位置的参数集合的元素类型
     *
     * @param method method
     * @param index 要去获取的参数的类型
     * @return 元素类型
     */
    @JvmStatic
    fun getComponentType(method: Method, index: Int): Class<*> {
        val type = method.parameterTypes[index]
        if (type.isArray) {
            return type.componentType
        } else {
            val genericType = method.genericParameterTypes[index]
            if (genericType != null) {
                return (genericType as ParameterizedType).actualTypeArguments[0] as Class<*>
            } else {
                val superGenericType = type.genericSuperclass
                if (superGenericType != null && superGenericType is ParameterizedType) {
                    return superGenericType.actualTypeArguments[0] as Class<*>
                }
            }
        }
        throw IllegalStateException("Cannot get componentType")
    }

    @JvmStatic
    fun getSetterMethods(clazz: Class<*>): List<Method> {
        return emptyList()
    }
}