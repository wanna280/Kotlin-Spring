package com.wanna.framework.core

import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

/**
 * 这是一个基于Kotlin反射的参数名发现器，负责获取Kotlin的函数/构造器的参数名列表；
 * 它可以去获取Kotlin类/接口的所有方法的参数名列表，因为Kotlin类当中的将所有的类的元信息都存储到@Metadata注解当中。
 * 因此Kotlin类，对于类的各个信息，都能通过Kotlin反射的方式去进行获取。
 *
 * @see kotlin.reflect.jvm.kotlinFunction
 */
open class KotlinReflectionParameterNameDiscoverer : ParameterNameDiscoverer {
    override fun getParameterNames(constructor: Constructor<*>): Array<String>? {
        if (KotlinDetector.isKotlinType(constructor.declaringClass)) {
            return doGetParameterNames(constructor)
        }
        return null
    }

    override fun getParameterNames(method: Method): Array<String>? {
        if (KotlinDetector.isKotlinType(method.declaringClass)) {
            return doGetParameterNames(method)
        }
        return null
    }

    /**
     * 给定具体的方法/构造器，去进行参数名的发现
     *
     * @param executable 要获取参数名的方法/构造器
     * @return 该方法或者构造器当中的参数名列表；如果获取不到对应的Kotlin的KFunction，那么return null
     */
    private fun doGetParameterNames(executable: Executable): Array<String>? {
        val kFunction: KFunction<*>? =
            if (executable is Method) executable.kotlinFunction else (executable as Constructor<*>).kotlinFunction
        return kFunction?.parameters?.mapNotNull { it.name }?.toTypedArray()
    }
}