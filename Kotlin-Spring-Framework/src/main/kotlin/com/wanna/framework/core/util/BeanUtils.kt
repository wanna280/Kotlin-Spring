package com.wanna.framework.core.util

import com.wanna.framework.core.DefaultParameterNameDiscoverer
import java.beans.ConstructorProperties
import java.lang.reflect.Constructor

object BeanUtils {

    // 参数名发现器，提供方法/构造器当中的参数名的获取
    private val parameterNameDiscoverer = DefaultParameterNameDiscoverer()

    /**
     * 通过有参数构造器去创建对象
     * @param ctor 构造器
     * @param args 参数列表
     */
    @JvmStatic
    fun <T> instantiateClass(ctor: Constructor<T>, vararg args: Any?): T {
        return ctor.newInstance(*args)
    }

    /**
     * 通过无参数构造器去创建对象
     * @param ctor 无参构造器
     */
    @JvmStatic
    fun <T> instantiateClass(ctor: Constructor<T>): T {
        return ctor.newInstance()
    }

    /**
     * 通过无参数构造器创建对象
     */
    @JvmStatic
    fun <T> instantiateClass(clazz: Class<T>): T {
        return clazz.getDeclaredConstructor().newInstance()
    }

    /**
     * 获取一个构造器的参数名列表；
     * 如果存在有JDK当中提供的@ConstructorProperties注解，那么从它上面去找；
     * 如果没有@ConstructorProperties注解，那么使用DefaultParameterNameDiscoverer去进行寻找
     *
     * @param ctor 要获取参数名的目标构造器
     * @throws IllegalStateException 如果没有找到合适的参数名列表/找到的参数名列表长度不对
     */
    fun getParameterNames(ctor: Constructor<*>): Array<String> {
        val cp = ctor.getAnnotation(ConstructorProperties::class.java)
        val parameterNames: Array<String>? =
            cp?.value ?: parameterNameDiscoverer.getParameterNames(ctor)
        if (parameterNames == null) {
            throw IllegalStateException("无法从目标构造器[ctor=$ctor]上获取到参数名列表")
        }
        if (parameterNames.size != ctor.parameterCount) {
            throw IllegalStateException("匹配到的参数名的数量和目标构造器的参数数量不相同")
        }
        return parameterNames
    }
}