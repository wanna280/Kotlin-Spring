package com.wanna.framework.core.annotation

import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.framework.lang.Nullable
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * 合成的用于生成注解的InvocationHandler，在Spring当中需要用到自定义的注解的合成，
 * 合成注解时，需要使用到JDK的动态代理，从而最终使用到InvocationHandler
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 * @see InvocationHandler
 *
 * @param annotation 合成的注解MergedAnnotation对象
 * @param type 原始的注解的类型
 * @param A  原始注解的类型
 */
class SynthesizedMergedAnnotationInvocationHandler<A : Annotation>
    (private val annotation: MergedAnnotation<A>, private val type: Class<A>) : InvocationHandler {

    companion object {
        /**
         * 为给定的注解对象去创建代理对象，用来去合成一个代理出来的注解对象
         *
         * @param annotation MergedAnnotation
         * @param type annotationType
         * @param A annotationType
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <A : Annotation> createProxy(annotation: MergedAnnotation<A>, type: Class<A>): A {
            return Proxy.newProxyInstance(
                type.classLoader,
                arrayOf(type),
                SynthesizedMergedAnnotationInvocationHandler(annotation, type)
            ) as A
        }
    }

    /**
     * 为给定的注解类型去构建出来该注解的属性方法
     */
    private val attributes = AttributeMethods.forAnnotationType(type)

    /**
     * hashCode
     */
    @Nullable
    @Volatile
    private var hashCode: Int? = null

    /**
     * 拦截注解对象的一些目标方法，需要去进行执行的回调方法
     *
     * @param proxy proxy代理对象
     * @param method 拦截到的目标方法
     * @param args 执行目标方法需要用到的参数列表
     */
    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        // 1.如果是equals方法
        if (ReflectionUtils.isEqualsMethod(method)) {
            return annotationEquals(proxy)
        }
        // 2.如果是HashCode方法
        if (ReflectionUtils.isHashCodeMethod(method)) {
            return annotationHashCode()
        }
        // 3.如果是toString方法
        if (ReflectionUtils.isToStringMethod(method)) {
            return annotationToString()
        }
        // 4.如果是annotationType方法
        if (isAnnotationTypeMethod(method)) {
            return this.type
        }
        // 5.如果是注解属性方法
        if (attributes.indexOf(method.name) != -1) {
            return getAttributeValue(method)
        }
        throw IllegalStateException("方法[$method]并不被合成注解[$type]所支持")
    }

    /**
     * 注解的"equals"比较
     *
     * @param other 别的注解
     */
    private fun annotationEquals(other: Any?): Boolean {
        other ?: return false
        if (this == other) {
            return true
        }
        return false
    }


    private fun annotationHashCode(): Int {
        return 0  // TODO
    }

    private fun annotationToString(): String {
        return this.annotation.toString()  // TODO
    }

    private fun isAnnotationTypeMethod(method: Method): Boolean {
        return method.name == "annotationType" && method.parameterCount == 0
    }

    private fun getAttributeValue(method: Method): Any {
        return ""  // TODO
    }
}