package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.ParameterNameDiscoverer
import java.lang.reflect.Field
import java.lang.reflect.Type

/**
 * 这是一个依赖的描述符，可以描述一个方法的参数，或者是一个字段，当然，也可以是一个构造器的参数也是可以的
 * 在Spring当中需要去进行依赖的解析时，就会将依赖的相关信息都封装成为一个DependencyDescriptor，方便BeanFactory当中可以对依赖去进行解析工作
 */
open class DependencyDescriptor
private constructor(
    private val field: Field?,
    private val parameter: MethodParameter?,
    private val required: Boolean,
    private val eager: Boolean = false
) {
    constructor(field: Field?, required: Boolean) : this(field, null, required)
    constructor(field: Field?, required: Boolean, eager: Boolean) : this(field, null, required, eager)
    constructor(parameter: MethodParameter?, required: Boolean) : this(null, parameter, required)
    constructor(parameter: MethodParameter?, required: Boolean, eager: Boolean) : this(null, parameter, required, eager)

    private var declaringClass: Class<*> = if (field != null) field.declaringClass else parameter!!.getDeclaringClass()

    // 字段名(描述的是一个字段时才生效)
    private var fieldName: String? = field?.name

    // 方法的参数类型列表(描述的是一个参数时才生效)
    private var parameterTypes: Array<Class<*>>? = parameter?.getParameterTypes()

    // 方法名(描述的是一个方法时才生效)
    private var methodName: String? = parameter?.getMethod()?.name

    // 参数所在的索引，默认为0
    private var parameterIndex: Int = parameter?.getParameterIndex() ?: 0

    // containingClass
    private var containingClass: Class<*>? = parameter?.getContainingClass()

    // 参数名发现器
    private var parameterNameDiscoverer: ParameterNameDiscoverer? = null

    /**
     * 获取方法参数，如果这描述的是一个字段，那么return null
     */
    open fun getMethodParameter(): MethodParameter? = this.parameter

    /**
     * 获取方法参数/构造器参数/字段上的注解列表
     */
    open fun getAnnotations(): Array<Annotation> =
        if (field != null) field.annotations else parameter!!.getAnnotations()

    /**
     * 获取指定的注解，如果是一个方法参数，那么从方法参数当中获取注解；如果是一个字段，从字段当中获取注解
     */
    open fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        return if (field != null) field.getAnnotation(annotationClass) else parameter!!.getAnnotation(annotationClass)
    }

    /**
     * 初始化参数名发现器
     *
     * @param parameterNameDiscoverer 要指定的ParameterNameDiscoverer；可以为null
     */
    open fun initParameterNameDiscoverer(parameterNameDiscoverer: ParameterNameDiscoverer?) {
        this.parameterNameDiscoverer = parameterNameDiscoverer
    }

    /**
     * 获取泛型的类型，如果是一个方法参数，那么获取方法参数的泛型；如果是一个字段，那么获取字段的泛型类型
     */
    open fun getGenericType(): Type = parameter?.getGenericParameterType() ?: field!!.genericType

    // 获取containingClass
    open fun getContainingClass(): Class<*>? = containingClass

    open fun setContainingClass(containingClass: Class<*>?) {
        this.containingClass = containingClass
    }

    // 获取参数所在的索引，如果描述的是字段的话，值为0
    open fun getParameterIndex(): Int = parameterIndex

    // 如果描述的是一个方法的话，返回方法名，如果描述的不是一个方法的话，返回null
    open fun getMethodName(): String? = methodName

    // 获取方法/字段/构造器所在的定义的类
    open fun getDeclaringClass(): Class<*> = declaringClass

    // 获取方法的参数类型列表，如果描述的不是一个方法，那么return null
    open fun getParameterTypes(): Array<Class<*>>? = parameterTypes

    // 获取字段名，如果它根本不是一个字段，return null
    open fun getFieldName(): String? = fieldName

    // 该依赖，是否是必要的？(required=true？)
    open fun isRequired() = required
    open fun isEager(): Boolean = eager

    /**
     * 返回依赖的类型，如果是字段返回字段类型，如果是方法参数返回方法参数的类型
     */
    open fun getDependencyType(): Class<*> = parameter?.getParameterType() ?: field!!.type

    /**
     * 提供解析候选Bean的方式，默认实现为从容器中获取
     */
    open fun resolveCandidate(beanName: String, requiredType: Class<*>, beanFactory: BeanFactory): Any? =
        beanFactory.getBean(beanName)
}