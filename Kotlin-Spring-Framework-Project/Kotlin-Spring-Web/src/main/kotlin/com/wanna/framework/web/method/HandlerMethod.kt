package com.wanna.framework.web.method

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.method.support.HandlerMethodUtil
import java.lang.reflect.Method

/**
 * 这是一个HandlerMethod，内部包装了要执行的Handler的Bean对象，Handler方法等
 */
open class HandlerMethod {

    // bean or beanName
    var bean: Any? = null

    // beanFactory
    var beanFactory: BeanFactory? = null

    // beanType
    var beanType: Class<*>? = null

    var handlerMethod: HandlerMethod? = null

    // 获取当前的HandlerMethod的解析之前的HandlerMethod(有可能现在变成了BeanObject，而之前是beanName)
    var resolvedFromHandlerMethod: HandlerMethod? = null

    // 方法的参数列表
    var parameters: Array<MethodParameter>? = null

    // 要去执行的目标Handler方法
    var method: Method? = null

    /**
     * 解析bean，如果bean还是beanName的话，需要从容器当中getBean
     *
     * @return 将beanName替换为Bean之后的新的HandlerMethod
     */
    fun createWithResolvedBean(): HandlerMethod {
        var handler = bean
        if (handler is String) {
            handler = this.beanFactory!!.getBean(handler)
        }
        val newHandlerMethod = newHandlerMethod(this, handler!!)
        newHandlerMethod.resolvedFromHandlerMethod = this  // set ResolvedFromHandlerMethod
        return newHandlerMethod
    }

    /**
     * 获取ReturnValue的MethodParameter
     *
     * @param returnValue 方法处理的最终返回值
     * @return 方法的返回值类型封装的MethodParameter
     */
    fun getReturnValueType(returnValue: Any?): MethodParameter {
        return ReturnValueMethodParameter(returnValue)
    }

    /**
     * 判断方法的返回值是否是void
     */
    fun isVoid(): Boolean {
        return Void.TYPE == method!!.returnType
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HandlerMethod

        if (bean != other.bean) return false
        if (method != other.method) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bean?.hashCode() ?: 0
        result = 31 * result + (method?.hashCode() ?: 0)
        return result
    }

    open inner class HandlerMethodParameter(index: Int) : MethodParameter(this@HandlerMethod.method!!, index) {
        override fun getMethodAnnotations(): Array<Annotation> {
            return this@HandlerMethod.method!!.annotations
        }

        override fun getContainingClass(): Class<*>? {
            return this@HandlerMethod.beanType
        }


    }

    /**
     * 这是对HandlerMethod的返回值的参数封装，让它能够适配到MethodParameter
     *
     * @param returnValue 方法的返回值
     */
    open inner class ReturnValueMethodParameter(private val returnValue: Any?) : HandlerMethodParameter(-1) {
        override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? =
            AnnotatedElementUtils.getMergedAnnotation(method!!, annotationClass)

        /**
         * 获取方法的返回值，如果返回值不为空，那么使用返回值的类型；如果返回值为空，那么直接使用"method.returnType"
         *
         * @return 方法的返回值类型
         */
        override fun getParameterType(): Class<*> =
            if (returnValue == null) method!!.returnType else returnValue::class.java
    }


    companion object {
        @JvmStatic
        fun newHandlerMethod(handlerMethod: HandlerMethod, handler: Any): HandlerMethod {
            return HandlerMethodUtil.newHandlerMethod(handlerMethod, handler, HandlerMethod::class.java)
        }

        @JvmStatic
        fun newHandlerMethod(handlerMethod: HandlerMethod): HandlerMethod {
            return HandlerMethodUtil.newHandlerMethod(handlerMethod, HandlerMethod::class.java)
        }

        @JvmStatic
        fun newHandlerMethod(beanFactory: BeanFactory, beanName: String, method: Method): HandlerMethod {
            return HandlerMethodUtil.newHandlerMethod(beanFactory, beanName, method, HandlerMethod::class.java)
        }

        @JvmStatic
        fun newHandlerMethod(bean: Any, method: Method): HandlerMethod {
            return HandlerMethodUtil.newHandlerMethod(bean, method, HandlerMethod::class.java)
        }
    }
}