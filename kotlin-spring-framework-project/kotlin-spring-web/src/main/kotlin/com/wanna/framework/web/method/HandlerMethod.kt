package com.wanna.framework.web.method

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import java.lang.reflect.Method
import java.util.*

/**
 * [HandlerMethod]内部包装了要执行的Handler的Bean对象以及Handler方法等信息
 *
 * @see com.wanna.framework.web.method.support.InvocableHandlerMethod
 */
open class HandlerMethod() {

    /**
     * bean or beanName
     */
    var bean: Any? = null

    /**
     * beanFactory
     */
    @Nullable
    var beanFactory: BeanFactory? = null

    /**
     * beanType
     */
    var beanType: Class<*>? = null

    var handlerMethod: HandlerMethod? = null

    /**
     * 获取当前的HandlerMethod的解析之前的HandlerMethod(有可能现在变成了BeanObject，而之前是beanName)
     */
    @Nullable
    var resolvedFromHandlerMethod: HandlerMethod? = null

    /**
     * 当前HandlerMethod对应方法的参数列表
     */
    var parameters: Array<MethodParameter>? = null

    /**
     * 要去执行的目标Handler方法
     */
    var method: Method? = null

    /**
     * 提供基于已经有的[HandlerMethod]去进行构建新的[HandlerMethod]
     *
     * @param handlerMethod 已经有的handlerMethod
     */
    constructor(handlerMethod: HandlerMethod) : this() {
        this.method = handlerMethod.method
        this.parameters = handlerMethod.parameters
        this.beanType = handlerMethod.beanType
        this.beanFactory = handlerMethod.beanFactory
        this.handlerMethod = handlerMethod
        this.bean = handlerMethod.bean
    }

    /**
     * 提供一个基于BeanFactory、beanName以及Method的构造器,
     * 暂时存放beanName, 在进行后续的解析时再真正地去进行Bean的解析
     *
     * @param beanFactory beanFactory
     * @param handler handler(beanName)
     * @param method method
     */
    constructor(beanFactory: BeanFactory, handler: String, method: Method) : this() {
        this.beanFactory = beanFactory
        this.bean = handler
        this.method = method
        this.parameters = Array(method.parameterCount) { MethodParameter(method, it) }
        val type = beanFactory.getType(handler)
            ?: throw IllegalStateException("Cannot resolve bean type for bean with name '$handler'")
        this.beanType = ClassUtils.getUserClass(type)
    }

    /**
     * 提供一个直接基于bean和Method去进行构建的构造器
     *
     * @param bean bean
     * @param method method
     */
    constructor(bean: Any, method: Method) : this() {
        this.bean = bean
        this.method = method

        // 初始化parameters和beanType
        this.parameters = Array(method.parameterCount) { MethodParameter(method, it) }
        this.beanType = ClassUtils.getUserClass(bean)
    }


    /**
     * HandlerMethod的描述信息(计算属性)
     */
    val description: String
        get() = initDescription(beanType!!, method!!)

    /**
     * 解析bean，如果bean还是beanName的话，需要从容器当中getBean
     *
     * @return 将beanName替换为Bean之后的新的HandlerMethod
     */
    open fun createWithResolvedBean(): HandlerMethod {
        var handler = bean
        if (handler is String) {
            handler = this.beanFactory!!.getBean(handler)
        }
        val newHandlerMethod = HandlerMethod(this)
        newHandlerMethod.bean = handler
        newHandlerMethod.resolvedFromHandlerMethod = this  // set ResolvedFromHandlerMethod
        return newHandlerMethod
    }

    /**
     * 获取ReturnValue的MethodParameter
     *
     * @param returnValue 方法处理的最终返回值
     * @return 方法的返回值类型封装的MethodParameter
     */
    open fun getReturnValueType(returnValue: Any?): MethodParameter {
        return ReturnValueMethodParameter(returnValue)
    }

    /**
     * 获取方法上的注解
     *
     * @return 获取到的该方法上的注解，如果该方法上获取不到该注解，那么return null
     */
    open fun <T : Annotation> getMethodAnnotation(annotationType: Class<T>): T? {
        return AnnotatedElementUtils.getMergedAnnotation(this.method!!, annotationType)
    }

    /**
     * 判断方法上是否存在某种类型的注解？
     *
     * @param annotationClass 要去进行匹配的注解类型
     * @return 如果该方法上有该注解，那么return true；否则return false
     */
    open fun hasMethodAnnotation(annotationClass: Class<out Annotation>): Boolean {
        return AnnotatedElementUtils.isAnnotated(this.method!!, annotationClass)
    }

    /**
     * 判断方法的返回值是否是void
     */
    open fun isVoid(): Boolean {
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

    /**
     * toString, 使用Description去进行返回
     *
     * @return toString
     */
    override fun toString(): String = this.description


    /**
     * 对于一个HandlerMethod的一个方法参数的封装，因为使用的是内部类的方式，它完全可以获取到外部类当中的HandlerMethod对象
     *
     * @param index 该参数位于方法当中的位置(对于返回值类型，那么index=-1)
     */
    open inner class HandlerMethodParameter(index: Int) : MethodParameter(this@HandlerMethod.method!!, index) {
        override fun getMethodAnnotations(): Array<Annotation> {
            return this@HandlerMethod.method!!.annotations
        }

        override fun getContainingClass(): Class<*> {
            return this@HandlerMethod.beanType!!
        }

        override fun <T : Annotation> getMethodAnnotation(annotationClass: Class<T>): T? {
            return this@HandlerMethod.getMethodAnnotation(annotationClass)
        }

        override fun hasMethodAnnotation(annotationClass: Class<out Annotation>): Boolean {
            return this@HandlerMethod.hasMethodAnnotation(annotationClass)
        }
    }

    /**
     * 这是对HandlerMethod的返回值的参数封装，让它能够适配到MethodParameter，
     * 并且匹配注解时，应该采用原始的HandlerMethod上的注解去进行匹配
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

        /**
         * 构建一个[HandlerMethod]的描述信息
         *
         * @param method method
         * @param beanType beanType
         * @return 对一个HandlerMethod的描述信息
         */
        @JvmStatic
        private fun initDescription(beanType: Class<*>, method: Method): String {
            val joiner = StringJoiner(", ", "(", ")")
            for (paramType in method.parameterTypes) {
                joiner.add(paramType.simpleName)
            }
            return beanType.name + "#" + method.name + joiner.toString()
        }
    }
}