package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.InjectionPoint
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.ParameterNameDiscoverer
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.lang.Nullable
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 这是一个依赖的描述符, 可以描述一个方法的参数, 或者是一个字段, 当然, 也可以是一个构造器的参数也是可以的
 * 在Spring当中需要去进行依赖的解析时, 就会将依赖的相关信息都封装成为一个DependencyDescriptor, 方便BeanFactory当中可以对依赖去进行解析工作
 *
 * @param required 该依赖是否是必须的?
 * @param eager 解析依赖的时候, 是否允许依赖被eagerInit(比如FactoryBean被提前创建)
 */
open class DependencyDescriptor protected constructor(
    field: Field?, parameter: MethodParameter?, private val required: Boolean, private val eager: Boolean = false
) : InjectionPoint(field, parameter) {

    /**
     * 基于另外一个[DependencyDescriptor]去创建一个新的[DependencyDescriptor]对象, 把相关的字段全部拷贝一份
     *
     * @param descriptor 另外的DependencyDescriptor
     */
    constructor(descriptor: DependencyDescriptor) : this(
        descriptor.getField(),
        descriptor.getMethodParameter(),
        descriptor.required,
        descriptor.eager
    ) {
        // copy declaringClass
        this.declaringClass = descriptor.declaringClass
        // copy methodName
        this.methodName = descriptor.methodName

        // copy parameterTypes
        this.parameterTypes = descriptor.parameterTypes
        // copy parameterIndex
        this.parameterIndex = descriptor.parameterIndex

        // copy fieldName
        this.fieldName = descriptor.fieldName

        // copy containingClass
        this.containingClass = descriptor.containingClass
        // copy nestedLevel
        this.nestingLevel = descriptor.nestingLevel
    }

    constructor(@Nullable field: Field?, required: Boolean) : this(field, null, required)
    constructor(@Nullable field: Field?, required: Boolean, eager: Boolean) : this(field, null, required, eager)
    constructor(@Nullable parameter: MethodParameter?, required: Boolean) : this(null, parameter, required)
    constructor(@Nullable parameter: MethodParameter?, required: Boolean, eager: Boolean) : this(
        null,
        parameter,
        required,
        eager
    )

    /**
     * 字段/方法参数被定义的类
     */
    private var declaringClass: Class<*>? = if (field != null) field.declaringClass else parameter?.getDeclaringClass()

    /**
     * 字段名(描述的是一个字段时才生效)
     */
    private var fieldName: String? = field?.name

    /**
     * 方法的参数类型列表(描述的是一个参数时才生效)
     */
    private var parameterTypes: Array<Class<*>>? = parameter?.getParameterTypes()

    /**
     * 方法名(描述的是一个方法时才生效)
     */
    private var methodName: String? = parameter?.getMethod()?.name

    /**
     * 方法参数所在的索引, 默认为0
     */
    private var parameterIndex: Int = parameter?.getParameterIndex() ?: 0

    /**
     * containingClass(方法参数对应的方法所在的类的具体实现类, containingClass可能为declaringClass的子类)
     */
    private var containingClass: Class<*>? = parameter?.getContainingClass()

    /**
     * 参数名发现器
     *
     * @see initParameterNameDiscoverer
     */
    private var parameterNameDiscoverer: ParameterNameDiscoverer? = null

    /**
     * 当前泛型的嵌套层级
     *
     * @see increaseNestingLevel
     */
    private var nestingLevel: Int = 1

    /**
     * 可以解析的类型ResolvableType(缓存, 使用时自动根据Field/MethodParameter去解析)
     *
     * @see getResolvableType
     */
    private var resolvableType: ResolvableType? = null

    /**
     * 初始化参数名发现器
     *
     * @param parameterNameDiscoverer 要指定的ParameterNameDiscoverer; 可以为null
     */
    open fun initParameterNameDiscoverer(@Nullable parameterNameDiscoverer: ParameterNameDiscoverer?) {
        if (parameterNameDiscoverer != null) {
            this.parameterNameDiscoverer = parameterNameDiscoverer
        }
    }

    /**
     * 是否允许Fallback的匹配? 默认为不允许
     *
     * @return 如果允许Fallback的匹配的话, 那么return true; 如果不允许的话, 那么return false(默认)
     */
    open fun fallbackMatchAllowed(): Boolean = false

    /**
     * 获得一个允许去进行fallback的匹配的[DependencyDescriptor]
     *
     * @return Fallback的DependencyDescriptor(重写了fallbackMatchAllowed方法, 允许去进行fallback的匹配)
     */
    open fun forFallbackMatch(): DependencyDescriptor {
        return object : DependencyDescriptor(this) {
            override fun fallbackMatchAllowed(): Boolean = true
        }
    }

    /**
     * 获取泛型的类型, 如果是一个方法参数, 那么获取方法参数的泛型; 如果是一个字段, 那么获取字段的泛型类型
     *
     * @return genericType
     */
    open fun getGenericType(): Type {
        val parameter = getMethodParameter()
        val field = getField()
        if (parameter != null) {
            return parameter.getGenericParameterType()
        }
        if (field != null) {
            return field.genericType
        }
        return null!!
    }

    /**
     * 获取containingClass(方法参数所定义的方法所在的类)
     *
     * @return containingClass
     */
    open fun getContainingClass(): Class<*>? = containingClass

    open fun setContainingClass(@Nullable containingClass: Class<*>?) {
        this.containingClass = containingClass
    }

    /**
     * 获取参数所在的索引, 如果描述的是字段的话, 值为0
     *
     * @return 参数索引index
     */
    open fun getParameterIndex(): Int {
        return parameterIndex
    }

    /**
     * 如果描述的是一个方法的话, 返回方法名, 如果描述的不是一个方法的话, 返回null
     *
     * @return methodName(如果描述的是字段的话, 值为null)
     */
    open fun getMethodName(): String? {
        return methodName
    }

    /**
     * 获取方法/字段/构造器所在的定义的类
     *
     * @return 方法/字段/构造器所在的定义的类
     */
    open fun getDeclaringClass(): Class<*> {
        return declaringClass!!
    }

    /**
     * 获取方法的参数类型列表, 如果描述的不是一个方法, 那么return null
     *
     * @return 参数类型列表
     */
    open fun getParameterTypes(): Array<Class<*>>? {
        return parameterTypes
    }

    /**
     * 获取字段名, 如果它根本不是一个字段, return null
     *
     * @return fieldName(or null)
     */
    open fun getFieldName(): String? {
        return fieldName
    }

    /**
     * 该依赖, 是否是必要的? (required=true? )
     *
     * @return required?
     */
    open fun isRequired(): Boolean {
        if (!required) {
            return false
        }
        return true
    }

    open fun isEager(): Boolean = eager

    /**
     * 获取依赖的名称
     *
     * @return 依赖名称(字段名/方法参数名)
     */
    @Nullable
    open fun getDependencyName(): String? {
        return getField()?.name ?: obtainMethodParameter().getParameterName()
    }

    /**
     * 返回依赖的类型, 如果是字段返回字段类型, 如果是方法参数返回方法参数的类型
     *
     * @return 待解析的依赖类型
     */
    open fun getDependencyType(): Class<*> {
        // 模拟com.wanna.framework.core.MethodParameter.getNestedParameterType, 去解析嵌套的泛型参数类型
        val field = getField()
        if (field != null) {
            // 嵌套层级＞1, 需要解析嵌套的泛型参数类型
            if (this.nestingLevel > 1) {
                var type = field.genericType
                for (i in 2..nestingLevel) {
                    if (type is ParameterizedType) {
                        val args = type.actualTypeArguments
                        type = args[args.size - 1]
                    }
                }
                if (type is Class<*>) {
                    return type
                } else if (type is ParameterizedType) {
                    val rawType = type.rawType
                    if (rawType is Class<*>) {
                        return rawType
                    }
                }
                return Any::class.java

                // 如果嵌套层级为1, 那么直接返回fieldType
            } else {
                return field.type
            }
        } else {
            return obtainMethodParameter().getNestedParameterType()
        }
    }

    /**
     * 提供解析候选Bean的方式, 默认实现为从给定的beanFactory当中去进行获取
     *
     * @param beanName beanName
     * @param beanFactory beanFactory
     * @param requiredType 类型
     */
    open fun resolveCandidate(beanName: String, requiredType: Class<*>, beanFactory: BeanFactory): Any {
        return requiredType.cast(beanFactory.getBean(beanName))
    }

    /**
     * 增加泛型参数的嵌套级别, 比如List<String>, 最开始在List, 嵌套层级增加之后就在String...
     */
    open fun increaseNestingLevel() {
        // nestingLevel++, 为了获取resolvableType时可以到深层次嵌套的泛型参数
        this.nestingLevel++
        // 把resolvableType去重置为null, 懒加载机制, 在调用getResolvableType方法时, 根据nestedLevel去进行重新初始化
        this.resolvableType = null
        // 让methodParameter的泛型嵌套层级++
        this.getMethodParameter()?.nested()
    }

    /**
     * 获取到该依赖描述符的可以解析的类型ResolvableType, 自动根据Field/MethodParameter去进行解析
     *
     * @return 依赖描述符的ResolvableType
     */
    open fun getResolvableType(): ResolvableType {
        val field = getField()
        var resolvableType = this.resolvableType
        if (resolvableType == null) {
            if (field != null) {
                // 为当前的嵌套级别的泛型参数, 去构建ResolvableType
                resolvableType = ResolvableType.forField(field, nestingLevel, this.containingClass)
            } else {
                resolvableType = ResolvableType.forMethodParameter(getMethodParameter()!!)
            }
            this.resolvableType = resolvableType
        }
        return resolvableType
    }

}