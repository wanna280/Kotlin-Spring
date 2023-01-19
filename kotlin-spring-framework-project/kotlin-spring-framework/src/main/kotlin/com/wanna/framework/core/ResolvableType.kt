package com.wanna.framework.core

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import java.lang.reflect.*

/**
 * 这是一个可以被解析的类型, 是Spring对于一个Class的顶层的封装, 功能很强大;
 * 它支持去获取到泛型的类型, 以及获取到它的接口当中的泛型等情况
 */
open class ResolvableType() {

    /**
     * 当前Class, 就是普通的不带泛型的类
     */
    private var resolved: Class<*>? = null

    /**
     * type可能是Class/GenericArrayType/ParameterizedType/WildcardType
     */
    private var type: Type? = null

    /**
     * 当前类型的它的所有接口
     */
    private var interfaces: Array<ResolvableType>? = null

    /**
     * 当前类型的父类的类型
     */
    private var superType: ResolvableType? = null

    /**
     * TypeVariable的解析器
     */
    private var variableResolver: VariableResolver? = null

    /**
     * 当前类的泛型信息
     */
    private var generics: Array<ResolvableType>? = null

    constructor(@Nullable clazz: Class<*>?) : this() {
        this.resolved = clazz ?: Any::class.java
        this.type = resolved
    }

    /**
     * 根据给定的type去进行构建
     *
     * @param type type
     * @param variableResolver 解析泛型E/T/K/V的TypeVariable解析器
     */
    constructor(@Nullable type: Type?, @Nullable variableResolver: VariableResolver?) : this() {
        this.type = type
        this.variableResolver = variableResolver
        this.resolved = resolveClass()  // 解析Class
    }

    /**
     * 根据type去解析出来Class对象, 放方便去设置到resolved字段当中, 有可能解析不到return null
     *
     * @return 解析结果Class
     */
    private fun resolveClass(): Class<*>? {
        // 有可能确实就是没有type...直接return null
        this.type ?: return null

        var resolved: Class<*>? = null
        // type可能的类型为, Class/ParameterizedType/WildcardType/GenericArrayType/TypeVariable

        // 如果是最普通的Class的话, 解析结果就是Class
        if (this.type is Class<*>) {
            resolved = this.type as Class<*>

            // 如果是GenericArrayType(泛型数组), 那么我们去创建出来一个空的数组, 从而去解析到类型
        } else if (this.type is GenericArrayType) {
            val genericComponentType = (this.type as GenericArrayType).genericComponentType
            if (genericComponentType != null) {
                // TODO
                if (genericComponentType is ParameterizedType) {
                    return java.lang.reflect.Array.newInstance(genericComponentType.rawType as Class<*>, 0).javaClass
                }
                return java.lang.reflect.Array.newInstance(genericComponentType as Class<*>, 0).javaClass
            }

            // 对于更多别的类型的话...
        } else {
            resolved = this.resolveType().resolve()
        }
        return resolved
    }

    /**
     * 将当前类型转换为Map
     *
     * @return 转换为Map之后的类型
     */
    open fun asMap(): ResolvableType {
        return `as`(Map::class.java)
    }

    /**
     * 将当前类型转换为Collection之后的类型
     *
     * @return 转换为Collection之后的类型
     */
    open fun asCollection(): ResolvableType {
        return `as`(Collection::class.java)
    }

    /**
     * 将当前ResolvableType解析成为它的父类当中的某个类型
     *
     * @return 解析完成的ResolvableType
     */
    open fun `as`(clazz: Class<*>): ResolvableType {
        if (this == NONE) {
            return NONE
        }
        val resolved = resolve()
        // 如果解析不到, 或者给定的clazz就是this.resolved, 那么return this
        if (resolved == null || resolved == clazz) {
            return this
        }
        val interfaces = getInterfaces()
        // 遍历所有的泛型接口, 先去完成接口的构建工作...
        interfaces.forEach { itf ->
            val interfaceAsType = itf.`as`(clazz)
            if (interfaceAsType != NONE) {
                return interfaceAsType
            }
        }
        return getSuperType().`as`(clazz)
    }

    /**
     * 获取super类型, 如果没有解析过的话, 那么先去进行解析
     *
     * @return superType(ResolvableType)
     */
    open fun getSuperType(): ResolvableType {
        val resolved = resolve() ?: return NONE

        // 获取泛型的父类...
        val superclass = resolved.genericSuperclass ?: return NONE
        var superType = this.superType

        // 如果之前没有解析过, 那么去进行解析...
        if (superType == null) {
            superType = forType(superclass, this)
            this.superType = superType
        }
        return superType
    }

    /**
     * 获取某个ResolvableType的接口列表
     *
     * @return 该类的接口列表(ResolvableType)
     */
    open fun getInterfaces(): Array<ResolvableType> {
        val resolved = resolve() ?: return EMPTY_TYPES_ARRAY
        var interfaces = this.interfaces
        if (interfaces == null) {
            // 获取所有的泛型接口, 去进行构建成为ResolvableType, type有可能为ParameterizedType/Class
            // 如果一个接口有泛型的话, 那么它的类型会是ParameterizedType(rawType & actualTypeArguments)
            // 如果一个接口没有泛型的话, 那么它的类型会是Class
            val genericInterfaces = resolved.genericInterfaces
            interfaces = Array(genericInterfaces.size) { index ->
                val interfaceType = forType(genericInterfaces[index], this)
                interfaceType
            }
            this.interfaces = interfaces
        }
        return interfaces
    }

    open fun resolve(): Class<*>? {
        return this.resolved
    }

    /**
     * 获取到解析得到的真实的类型, 如果获取不到, 那么返回给定的fallback类型
     *
     * @param fallback fallback类型
     * @return 解析得到的类型Class
     */
    open fun resolve(fallback: Class<*>): Class<*> {
        return this.resolved ?: fallback
    }

    /**
     * 获取当前[ResolvableType]的原始类型
     *
     * @return rawClass(or null)
     */
    @Nullable
    open fun getRawClass(): Class<*>? {
        if (this.type == this.resolved) {
            return this.resolved
        }
        var rawType = this.type
        if (rawType is ParameterizedType) {
            rawType = rawType.rawType
        }
        return if (rawType is Class<*>) rawType else null
    }

    /**
     * 判断当前类型是否是一个数组? 有三种情况是匹配的
     * (1)type is Class, 并且type.isArray
     * (2)type is GenericArrayType
     * (3)解析类型(resolveType())解析出来是数组...
     *
     * @return 当前类型是否是一个数组
     */
    open fun isArray(): Boolean {
        val type = this.type
        return (type is Class<*> && type.isArray) || type is GenericArrayType || resolveType().isArray()
    }

    /**
     * 解析类型; (type类型可能为Class/ParameterizedType/WildcardType/GenericArrayType/TypeVariable)
     * (1)如果是Class的话, 直接去进行构建就行了...
     * (2)如果是ParameterizedType的话, 应该使用rawType作为真正的类型;
     * (3)如果是野生的泛型类型的话(java里的? extends, 或者是Kotlin里的*等情况), 需要去解析向上/向下转型
     * (4)如果是TypeVariable... 这种情况, 就是在解析接口当中的泛型的时候, 遇到了E/T这种不知道的类型,
     * 需要向最外层的类型当中去进行寻找(VariableResolver就负责包装外层的类型)
     *
     * Note: 这里不解析泛型数组的情况, 只会解析别的4种情况...
     * @return 解析成为的ResolvableType
     */
    open fun resolveType(): ResolvableType {
        if (type is Class<*>) {
            return forClass(this.type as Class<*>)
        }
        // 如果是有真实的泛型参数的话, 那么rawType就是原始的类型(比如List)
        if (this.type is ParameterizedType) {
            return forType((this.type!! as ParameterizedType).rawType, this.variableResolver)
        }

        // 如果是野生的泛型类型的话(java里的? extends, 或者是Kotlin里的*/out/in等情况), 需要检查向上/向下转型...
        // 这种情况比较好解决, 直接转型就完事了...
        if (this.type is WildcardType) {
            val wildcardType = this.type as WildcardType
            var resolved = resolveBounds(wildcardType.lowerBounds)
            if (resolved == null) {
                resolved = resolveBounds(wildcardType.upperBounds)
            }
            return forType(resolved, this.variableResolver)
        }
        // 如果是TypeVariable(例如泛型E时), 那么就会使用VariableResolver去进行解析
        // 比如原始是List<String>, 现在是Collection<E>, 解析的过程当中, 遇到了E这种情况, 就需要回到List<String>当中去寻找E的类型
        if (this.type is TypeVariable<*>) {
            val resolveVariable = this.variableResolver?.resolveVariable(this.type as TypeVariable<*>)
            if (resolveVariable != null) {
                return resolveVariable
            }
            return forType(resolveBounds((this.type as TypeVariable<*>).bounds), this.variableResolver)
        }
        return NONE
    }

    /**
     * 解析野生类型的向上转型和向下转型(比如"<out UserService>"/"<in UserService>")这种情况;
     * 内层的类型, 机会被封装到Bound数组当中, 可以通过upperBounds/lowerBounds等方式去进行获取
     *
     * @param bounds BoundTypes, 有可能是向上转型/向上转型的来的BoundTypes
     * @return 解析出来的泛型的上界/下界限
     */
    private fun resolveBounds(bounds: Array<Type>): Type? {
        if (bounds.isEmpty() || bounds[0] == Any::class.java) {
            return null
        }
        return bounds[0]
    }

    /**
     * 当前类型是否有泛型?
     */
    open fun hasGeneric(): Boolean {
        return getGenerics().isNotEmpty()
    }

    /**
     * 根据type, 去获取该type对应的泛型类型;
     * (1)如果该类型是Class, 那么获取它的typeParameters作为泛型类型(直接在类上写死的类型, 比如<User>, 没有T/E这类的)
     * (2)如果该类型是ParameterizedType, 那么获取它的actualTypeArguments作为泛型类型
     * (3)如果是别的几种类型的话, resolveType().getGenerics()
     *
     * @return type对应的泛型类型
     */
    open fun getGenerics(): Array<ResolvableType> {
        if (this == NONE) { // 不判断会递归无法结束, SOF
            return EMPTY_TYPES_ARRAY
        }
        var generics = this.generics
        if (generics == null) {
            if (type is Class<*>) {
                // 类身上的TypeParameter(有可能是<K,V>这种), 直接把K/V作为type去进行构建ResolvableType
                // 构建的过程当中会走到resolveClass->resolveType去进行TypeVariable的解析...
                val typeParameters = (type as Class<*>).typeParameters
                generics = Array(typeParameters.size) {
                    forType(typeParameters[it], this.variableResolver)
                }

                // 如果是ParameterizedType这种类型的话, 就比较简单, 直接获取到actualTypeArguments, 并去进行构建就行...
            } else if (type is ParameterizedType) {
                val actualTypeArguments = (type as ParameterizedType).actualTypeArguments
                generics = Array(actualTypeArguments.size) {
                    forType(actualTypeArguments[it], this.variableResolver)
                }

                // 如果是别的类型的话, 那么就得走resolveType了...
            } else {
                generics = resolveType().getGenerics()
            }
            this.generics = generics
        }
        return generics
    }

    /**
     * 使用[VariableResolver], 去解析出来那些未知的泛型的类型(T/E/K/V)
     *
     * @param typeVariable TypeVariable(T/E/K/V等)
     * @return 根据TypeVariable去解析出来的结果
     */
    open fun resolveVariable(typeVariable: TypeVariable<*>): ResolvableType? {
        // 只有原始的type为ParameterizedType才能去获取到真正的泛型信息...
        if (this.type is ParameterizedType) {
            val resolved = resolve() ?: return null

            // 获取到Class的类型参数, 比如List来说这里基于可以获取到"E", 对于Map来说这里可以获取到"K,V"
            val typeParameters = resolved.typeParameters

            // eg: 如果字段泛型为List<String>, 它的定义类型为List<E>, 那么它在进行接口的转换时, 会转换到的Collection<E>
            // 但是在解析Collection<E>的泛型时, 遇到了TypeVariable(E)这种情况, 就得使用VariableResolver去之前的List<String>当中去解析泛型类型
            // List<String>的泛型类型恰好是ParameterizedType...
            // 此事, 可以遍历List的所有TypeParameter(例如E), 和Collection的E去进行匹配, 最终泛型名称为E相等了, 因此就确定了
            // 真实的类型位于ParameterizedType的索引为index, 从而获取到真实的泛型类型E的具体类型为String

            typeParameters.indices.forEach {
                if (typeParameters[it].name == typeVariable.name) {
                    val actualType = (this.type as ParameterizedType).actualTypeArguments[it]
                    return forType(actualType, this.variableResolver)
                }
            }
        }
        return null
    }

    /**
     * 将一个ResolvableType转换成为[VariableResolver], 提供对于未知泛型的解析能力;
     * 这里只有在当前的[ResolvableType]的type是[ParameterizedType]时, 才能提供泛型的解析能力
     *
     * @return VariableResolver
     */
    open fun asVariableResolver(): VariableResolver? {
        return DefaultVariableResolver(this)
    }

    override fun toString(): String {
        return ClassUtils.getQualifiedName(resolved ?: Any::class.java)
    }


    companion object {
        /**
         * 空的ResolvableType
         */
        @JvmField
        val NONE = ResolvableType()

        /**
         * 空的类型(ResolvableType)数组
         */
        @JvmField
        val EMPTY_TYPES_ARRAY = emptyArray<ResolvableType>()

        /**
         * 给定type去进行构建ResolvableType
         *
         * @param type type(type的常见的子类型包括Class/ParameterizedType/WildcardType/GenericArrayType/TypeVariable这几个类别)
         */
        @JvmStatic
        fun forType(type: Type?, variableResolver: VariableResolver?): ResolvableType {
            return ResolvableType(type, variableResolver)
        }

        /**
         * 给定type去进行构建ResolvableType
         * (type的常见的子类型包括Class/ParameterizedType/WildcardType/GenericArrayType/TypeVariable这几个类别)
         *
         * @param type (Type有可能是带有泛型的ParameterizedType, 也可能只是普通的Class)
         * @param owner 这个ResolvableType的所有者, 可以从owner去转换成为VariableResolver完成K/V/E/T等泛型的解析
         * @return 转换得到的ResolvableType
         */
        @JvmStatic
        fun forType(type: Type?, owner: ResolvableType?): ResolvableType {
            var variableResolver: VariableResolver? = null
            if (owner != null) {
                variableResolver = owner.asVariableResolver()
            }
            if (type == null) {
                return NONE
            }
            // 如果类型是Class的话, 直接构建...
            if (type is Class<*>) {
                return ResolvableType(type)
            }
            // 如果是(ParameterizedType/WildcardType/GenericArrayType/TypeVariable)这几类的话...
            return forType(type, variableResolver)
        }

        /**
         * 为指定类型去构建ResolvableType(type=resolved=clazz)
         *
         * @param clazz 目标类
         * @return 根据Class构建的ResolvableType
         */
        @JvmStatic
        fun forClass(clazz: Class<*>): ResolvableType {
            return forType(type = clazz, variableResolver = null)
        }

        /**
         * 将一个字段的相关泛型信息去转换为[ResolvableType]
         *
         * @param field 目标字段
         * @return 用于去描述字段的ResolvableType
         */
        @JvmStatic
        fun forField(field: Field): ResolvableType {
            // 先构建出来一个type=ParameterizedType的ResolvableType去作为VariableResolver, 去提供泛型的解析
            val owner = forType(type = field.genericType, variableResolver = null)
            return forType(field.genericType, owner.asVariableResolver())
        }

        /**
         * 将一个方法参数去转换成为[ResolvableType]
         *
         * @param method 方法
         * @param index 方法参数index?
         * @return 解析得到描述该方法参数的ResolveType
         */
        @JvmStatic
        fun forMethodParameter(method: Method, index: Int): ResolvableType {
            return forMethodParameter(MethodParameter(method, index))
        }

        /**
         * 根据给定的方法的返回值去构建出来[ResolvableType]
         *
         * @param method method
         * @return 根据方法返回值, 去构建出来的ResolvableType
         */
        @JvmStatic
        fun forMethodReturnType(method: Method): ResolvableType {
            return forMethodParameter(MethodParameter(method, -1))
        }

        /**
         * 将一个方法参数类型去转换为ResolvableType
         *
         * @param methodParameter 方法参数(包装了jdk的Parameter)
         * @return 方法参数(来自jdk的Parameter)转换得到的ResolvableType
         */
        @JvmStatic
        fun forMethodParameter(methodParameter: MethodParameter): ResolvableType {
            // 先构建出来一个type=ParameterizedType的ResolvableType去作为VariableResolver, 去提供泛型的解析
            val owner = forType(type = methodParameter.getGenericParameterType(), variableResolver = null)
            return forType(methodParameter.getGenericParameterType(), owner.asVariableResolver())
        }

        /**
         * 在明确知道类的泛型的情况下, 可以直接去给定一个Class, 并且直接指定它的泛型的方式去快捷构建出来[ResolvableType]
         *
         * @param clazz 目标Class
         * @param generics 泛型列表
         * @return 携带有泛型的Class解析成为的ResolvableType
         */
        @JvmStatic
        fun forClassWithGenerics(clazz: Class<*>, vararg generics: Class<*>): ResolvableType {
            return forClassWithGenerics(forClass(clazz), *generics.map { forClass(it) }.toTypedArray())
        }

        /**
         * 给定目标类和该类的泛型信息泛型, 去构建[ResolvableType]
         *
         * @param resolvableType 目标类
         * @param generics 该类的泛型列表
         * @return ResolvableType
         */
        @JvmStatic
        private fun forClassWithGenerics(
            resolvableType: ResolvableType,
            vararg generics: ResolvableType
        ): ResolvableType {
            if (generics.isNotEmpty()) {
                resolvableType.generics = arrayOf(*generics)
            }
            return resolvableType
        }

        /**
         * 清除缓存
         */
        @JvmStatic
        fun clearCache() {

        }
    }

    /**
     * 这是一个TypeVariable的解析器(有可能在解析泛型的过程当中, 遇到了E/T/K/V等未知泛型的情况, 就得去进行解析)
     *
     * @see DefaultVariableResolver
     */
    interface VariableResolver {

        /**
         * 真正执行泛型的解析的对象
         *
         * @return source
         */
        fun getSource(): Any

        /**
         * 解析TypeVariable, 有可能在解析泛型的过程当中, 遇到了E/T/K/V等泛型的情况, 就得去进行解析
         *
         * @param typeVariable 解析过程当中遇到的TypeVariable
         * @return 将TypeVariable转换成为ResolvableType
         */
        fun resolveVariable(typeVariable: TypeVariable<*>): ResolvableType?
    }

    /**
     * 默认的TypeVariable解析器, 使用ResolvableType作为变量解析器
     *
     * @param source source of ResolvableType, 一般为最外层的类型...
     */
    open class DefaultVariableResolver(private val source: ResolvableType) : VariableResolver {
        override fun getSource() = this.source
        override fun resolveVariable(typeVariable: TypeVariable<*>) = this.source.resolveVariable(typeVariable)
    }
}