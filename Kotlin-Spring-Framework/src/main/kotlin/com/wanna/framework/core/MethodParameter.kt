package com.wanna.framework.core

import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * 这是对于一个方法的参数去进行的描述，通过参数索引(parameterIndex)，即可获取到方法的参数对象Parameter(来自java的reflect包)
 */
open class MethodParameter(
    private var executable: Executable,
    private var parameterIndex: Int,
    private var containingClass: Class<*>?,
    private var nestingLevel: Int = 1
) {

    constructor(executable: Executable, parameterIndex: Int) : this(executable, parameterIndex, null, 1)
    constructor(executable: Executable, parameterIndex: Int, nestingLevel: Int) : this(
        executable,
        parameterIndex,
        null,
        nestingLevel
    )

    constructor(executable: Executable, parameterIndex: Int, containingClass: Class<*>?) : this(
        executable,
        parameterIndex,
        containingClass,
        1
    )


    // 方法的泛型类型
    private var genericParameterType: Type? = null

    // 参数的类型
    private var parameterType: Class<*>? = null

    /**
     * 获取描述的方法参数上的全部注解列表
     */
    open fun getAnnotations(): Array<Annotation> {
        return executable.parameters[parameterIndex].annotations
    }

    /**
     * 获取描述的方法参数上的注解，找不到return null
     */
    open fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        return executable.parameters[parameterIndex].getAnnotation(annotationClass)
    }

    /**
     * 获取方法上的Annotation列表
     */
    open fun getMethodAnnotations(): Array<Annotation> {
        return executable.annotations
    }

    /**
     * 获取参数的返回类型
     */
    open fun getParameterType(): Class<*> {
        return executable.parameterTypes[parameterIndex]
    }

    /**
     * 获取参数对应的方法所在的index
     */
    open fun getParameterIndex(): Int {
        return parameterIndex
    }

    /**
     * 参数的定义类型
     */
    open fun getDeclaringClass(): Class<*> {
        return executable.declaringClass
    }

    /**
     * 获取包含的类
     */
    open fun getContainingClass(): Class<*>? {
        return containingClass
    }

    /**
     * 获取内部的级别
     */
    open fun getNestingLevel(): Int {
        return nestingLevel
    }

    /**
     * 返回参数的泛型类型
     */
    open fun getGenericParameterType(): Type {
        return executable.parameters[parameterIndex].parameterizedType
    }

    /**
     * 获取方法，如果包装的是构造器，则return null
     */
    open fun getMethod(): Method? = executable as? Method

    /**
     * 获取构造器，如果包装的是一个方法，那么返回null
     */
    open fun getConstructor(): Constructor<*>? = executable as? Constructor<*>

    /**
     * 获取Memember
     */
    open fun getMember(): Member = executable

    /**
     * 获取参数类型
     */
    open fun getParameterTypes(): Array<Class<*>> = executable.parameterTypes

    companion object {
        /**
         * 提供静态方法，为Executable去构建MethodParameter
         */
        @JvmStatic
        fun forExecutable(executable: Executable, parameterIndex: Int): MethodParameter {
            return MethodParameter(executable, parameterIndex)
        }
    }

}