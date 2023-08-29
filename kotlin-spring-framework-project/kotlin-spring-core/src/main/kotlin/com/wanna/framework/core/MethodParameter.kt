package com.wanna.framework.core

import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable
import java.lang.reflect.*

/**
 * 这是对于一个方法的参数去进行的描述, 通过参数索引(parameterIndex), 即可获取到方法/构造器的参数对象Parameter(来自java的reflect包);
 * Note: 特殊地, 它也可以被用来去描述一个方法的返回值等
 *
 * 例如, 对于一个方法"int foo(String str, Integer i, User u)"来说, 通过Method(Executable), 以及索引index=2, 即可获取到参数"u"对应的具体的描述信息;
 * 能获取到来自java的reflect包, 自然也能获取到该方法参数的泛型信息、注解信息等诸多信息
 *
 * @param executable 方法/字段/构造器
 * @param parameterIndex 参数的index, 必须介于[-1, parameterCount-1]之间, 并且index=-1代表描述的是方法的返回值
 * @param containingClass containingClass, 指定的方法的具体实现类, 如果不给这个参数的话, 那么认为containingClass就是declaringClass
 * @param nestingLevel 方法参数的嵌套层级, 用于解析泛型, 例如Map<String, String>在指定nestingLevel=2的情况下, 可以获取到内部的泛型参数
 * @param typeIndexesPerLevel 嵌套层级解析泛型参数时, 需要使用哪个位置的泛型参数
 */
open class MethodParameter(
    private var executable: Executable,
    private var parameterIndex: Int,
    private var containingClass: Class<*>?,
    private var nestingLevel: Int = 1,
    private var typeIndexesPerLevel: MutableMap<Int, Int>?
) {
    constructor(executable: Executable, parameterIndex: Int) : this(executable, parameterIndex, null, 1, null)
    constructor(executable: Executable, parameterIndex: Int, nestingLevel: Int) : this(
        executable,
        parameterIndex,
        null,
        nestingLevel,
        null
    )

    constructor(executable: Executable, parameterIndex: Int, containingClass: Class<*>?) : this(
        executable,
        parameterIndex,
        containingClass,
        1,
        null
    )

    /**
     * 对外提供一个用于copy的MethodParameter构造器
     *
     * @param original 原始的待进行copy的MethodParameter
     */
    constructor(original: MethodParameter) : this(
        original.executable,
        original.parameterIndex,
        original.containingClass,
        original.nestingLevel,
        original.typeIndexesPerLevel
    )

    init {
        val parameterCount = executable.parameterCount
        if (parameterIndex < -1 || parameterIndex > parameterCount) {
            throw IllegalStateException("parameterIndex必须介于[-1, ${parameterCount - 1}]之间, 并且parameterIndex=-1代表描述方法的返回值")
        }
    }

    /**
     * 参数名发现器, 提供该方法/构造器当中的方法的参数名列表的获取
     */
    private var parameterNameDiscoverer: ParameterNameDiscoverer? = null

    /**
     * 嵌套一层的MethodParameter(常用, 做一层缓存)
     */
    private var nestedMethodParameter: MethodParameter? = null

    /**
     * 初始化参数名发现器(Kotlin反射/标准反射/ASM三种方式)
     *
     * @param parameterNameDiscoverer 对于该方法名发现器, 去指定要使用的参数名发现器
     */
    open fun initParameterNameDiscoverer(parameterNameDiscoverer: ParameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer
    }

    /**
     * 获取描述的方法/构造器的参数上的全部注解列表
     *
     * @return 方法上的注解列表
     */
    open fun getAnnotations(): Array<Annotation> {
        return executable.parameters[parameterIndex].annotations
    }

    /**
     * 获取原始的typeIndexesPerLevel的Map
     *
     * @return typeIndexesPerLevel
     */
    @Nullable
    open fun getOriginTypeIndexesPerLevel(): Map<Int, Int>? {
        return typeIndexesPerLevel
    }

    /**
     * 获取懒加载的typeIndexesPerLevel Map, 如果之前不存在的话, 那么构建一个空的Map
     *
     * @return typeIndexesPerLevel
     */
    open fun getTypeIndexesPerLevel(): MutableMap<Int, Int> {
        if (this.typeIndexesPerLevel == null) {
            this.typeIndexesPerLevel = LinkedHashMap(4)
        }
        return this.typeIndexesPerLevel!!
    }

    /**
     * 获取描述的方法/构造器的参数上的注解列表
     *
     * @return 该方法/构造器上的注解列表
     */
    open fun getParameterAnnotations(): Array<Annotation> = executable.parameters[parameterIndex].annotations

    /**
     * 获取描述的方法参数/构造器参数上的注解, 找不到return null
     */
    open fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        return AnnotatedElementUtils.getMergedAnnotation(this.getParameter(), annotationClass)
    }

    /**
     * 获取方法上的某个类型的注解
     *
     * @param annotationClass 要去进行匹配的注解类型
     * @return 如果方法上找到了该注解, 那么返回该注解信息; 如果方法上没有找到该注解, 那么return null
     */
    open fun <T : Annotation> getMethodAnnotation(annotationClass: Class<T>): T? {
        return AnnotatedElementUtils.getMergedAnnotation(this.executable, annotationClass)
    }

    /**
     * 判断方法上是否有该注解? (支持使用继承的方式去进行寻找)
     *
     * @param annotationClass 要去进行匹配的注解类型
     * @return 如果方法上标注了该注解, 那么return true; 否则return false
     */
    open fun hasMethodAnnotation(annotationClass: Class<out Annotation>): Boolean {
        return AnnotatedElementUtils.hasAnnotation(this.executable, annotationClass)
    }

    /**
     * 获取方法/构造器上的Annotation列表
     *
     * @return 方法/构造器上直接标注的注解列表
     */
    open fun getMethodAnnotations(): Array<Annotation> {
        return executable.annotations
    }

    /**
     * 获取包装的参数的类型(如果parameterIndex<0, 那么代表方法的返回值; 如果parameterIndex>0, 那么代表方法参数)
     *
     * @return 方法参数/方法返回值的类型
     */
    open fun getParameterType(): Class<*> {
        // 如果parameterIndex<0, 那么代表的是使用方法的返回值
        if (parameterIndex < 0) {
            val method = this.getMethod() ?: return Void.TYPE
            return method.returnType
        }
        return executable.parameterTypes[parameterIndex]
    }

    /**
     * 获取方法参数(来自jdk的Parameter)对象, 将其暴露给使用者
     */
    open fun getParameter(): Parameter {
        if (parameterIndex < 0) {
            throw IllegalStateException("无法根据为方法的返回值, 去获取到Parameter")
        }
        return executable.parameters[parameterIndex]
    }

    /**
     * 直接获取Executable
     *
     * @return Executable(方法/构造器)
     */
    open fun getExecutable(): Executable = this.executable

    /**
     * 获取参数对应的方法所在的index
     */
    open fun getParameterIndex(): Int {
        return parameterIndex
    }

    /**
     * 该方法/构造器, 被定义在哪个类当中?
     *
     * @return 方法/构造器所被定义的类
     */
    open fun getDeclaringClass(): Class<*> {
        return executable.declaringClass
    }

    /**
     * 获取包含的类, 也就是方法的具体实现类(如果有指定containingClass, 那么使用自定义的; 如果没有自定义, 那么使用declaringClass作为containingClass)
     *
     * @return 方法的具体实现类
     */
    open fun getContainingClass(): Class<*> {
        return containingClass ?: getDeclaringClass()
    }

    /**
     * 获取内部的级别
     */
    open fun getNestingLevel(): Int {
        return nestingLevel
    }

    /**
     * 返回方法参数的泛型类型(如果parameterIndex<0, 那么代表方法的返回值; 如果parameterIndex>0, 那么代表方法参数)
     *
     * @return 获取方法参数的泛型类型(如果必要的话)
     */
    open fun getGenericParameterType(): Type {
        if (parameterIndex < 0) {
            val method = getMethod() ?: return Void.TYPE
            // 返回带有泛型的方法参数
            return method.genericReturnType
        }
        return executable.parameters[parameterIndex].parameterizedType
    }

    /**
     * 获取方法, 如果包装的是构造器, 则return null
     *
     * @return 将executable转换为方法, 如果它不是方法, return null
     */
    open fun getMethod(): Method? = executable as? Method

    /**
     * 获取构造器, 如果包装的是一个方法, 那么返回null
     *
     * @return 将executable转换为构造器, 如果它根本不是构造器, return null
     */
    open fun getConstructor(): Constructor<*>? = executable as? Constructor<*>

    /**
     * 获取Member, 也就是该参数对应的方法/构造器对象(executable)本身
     */
    open fun getMember(): Member = executable

    /**
     * 获取该参数对应的方法/构造器的参数类型列表
     *
     * @return 参数类型列表(Array<Class<*>>)
     */
    open fun getParameterTypes(): Array<Class<*>> = executable.parameterTypes

    /**
     * 如果有参数名发现器的话, 通过参数名发现器去获取参数名, 如果 想要获取参数名的话, 那么需要提前初始化参数名发现器
     *
     * @return 如果参数名发现器匹配了, 那么return参数名; 不然return null
     */
    open fun getParameterName(): String? {
        val nameDiscoverer = this.parameterNameDiscoverer
        val executable = this.executable
        var parameterNames: Array<String>? = null

        // 使用参数名解析器去解析到合适的参数名列表
        if (nameDiscoverer != null) {
            if (executable is Method) {
                parameterNames = nameDiscoverer.getParameterNames(executable)
            } else if (executable is Constructor<*>) {
                parameterNames = nameDiscoverer.getParameterNames(executable)
            }
        }
        // 如果参数名列表不为空, 那么根据parameterIndex去return 参数名
        if (parameterNames != null && parameterIndex >= 0) {
            return parameterNames[parameterIndex]
        }
        return null
    }

    /**
     * 获取当前方法参数, 嵌套一层的泛型方法参数
     *
     * @return 嵌套一层的泛型参数
     */
    open fun nested(): MethodParameter {
        return nested(null)
    }

    /**
     * 获取当前方法参数, 嵌套一层的泛型方法参数
     *
     * @param typeIndex 该层级要使用哪个位置的泛型参数? 不传默认取最后一个
     * @return 嵌套一层的泛型参数
     */
    open fun nested(@Nullable typeIndex: Int?): MethodParameter {
        var nestedParam = this.nestedMethodParameter
        // 如果type==null, 缓存起来单层嵌套的MethodParameter
        if (nestedParam != null && typeIndex == null) {
            return nestedParam
        }
        nestedParam = nested(this.nestingLevel + 1, typeIndex)
        if (typeIndex == null) {
            nestedMethodParameter = nestedParam
        }
        return nestedParam
    }

    /**
     * 根据当前方法参数, 以及嵌套层级信息, 重新去构建一个MethodParameter
     *
     * @param nestingLevel 最终的嵌套层级
     * @param typeIndex 该嵌套层级的泛型参数, 需要使用哪个位置的泛型, 指定泛型参数index?
     */
    open fun nested(nestingLevel: Int, typeIndex: Int?): MethodParameter {
        val copy = clone()

        // 修改嵌套层级
        copy.nestingLevel = nestingLevel
        // copy typeIndexesPerLevel
        if (this.typeIndexesPerLevel != null) {
            copy.typeIndexesPerLevel = LinkedHashMap(this.typeIndexesPerLevel)
        }
        if (typeIndex != null) {
            copy.getTypeIndexesPerLevel()[nestingLevel] = typeIndex
        }

        return copy
    }

    open fun clone(): MethodParameter {
        return MethodParameter(this)
    }


    companion object {
        /**
         * 提供静态方法, 为Executable去构建MethodParameter
         *
         * @param executable 方法/构造器
         * @param parameterIndex 当前参数位于该方法/构造器的第几个位置?
         * @return 为该方法参数构建好的MethodParameter对象
         */
        @JvmStatic
        fun forExecutable(executable: Executable, parameterIndex: Int): MethodParameter {
            return MethodParameter(executable, parameterIndex)
        }
    }

}