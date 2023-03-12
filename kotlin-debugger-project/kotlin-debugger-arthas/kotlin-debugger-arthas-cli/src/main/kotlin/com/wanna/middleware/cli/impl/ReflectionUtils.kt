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
     * 使用给定的类的无参数构造器, 去完成实例化
     *
     * @param clazz 待实例化的类
     * @return 实例化得到的目标对象
     *
     * @param T 目标对象类型
     */
    @JvmStatic
    fun <T> newInstance(clazz: Class<T>): T {
        return clazz.getDeclaredConstructor().newInstance()
    }

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

    /**
     * 从给定的类当中, 去提取到所有的Setter方法
     *
     * * Note: 获取的是所有的public方法, 对于private方法不算
     *
     * @param clazz clazz
     * @return 从该类当中去获取到的所有的Setter方法
     */
    @JvmStatic
    fun getSetterMethods(clazz: Class<*>): List<Method> {
        val setterMethods = ArrayList<Method>()
        val methods = clazz.methods
        for (method in methods) {
            if (isSetterMethod(method)) {
                setterMethods.add(method)
            }
        }
        return setterMethods
    }

    /**
     * 检查给定的方法, 是否是一个JavaBean的Setter方法
     *
     * @param method 待检查的方法
     * @return 如果该方法的方法名以"set"开头, 并且参数数量为1, 说明是Setter方法, return true; 否则return false
     */
    @JvmStatic
    fun isSetterMethod(method: Method): Boolean {
        return method.name.startsWith("set") && method.parameterCount == 1
    }
}