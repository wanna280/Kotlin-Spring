package com.wanna.framework.core

import java.io.Serializable
import java.lang.reflect.*

/**
 * 对于Java的Type提供解析的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/8/27
 * @see Type
 */
open class ResolvableType {

    /**
     * 当前Class, 也就是不带泛型的Class
     */
    private var resolved: Class<*>? = null

    /**
     * 需要进行解析的Type
     */
    private var type: Type? = null

    /**
     * 数组的元素类型, 如果无法推断出来, 那么值为null
     */
    private var componentType: ResolvableType? = null

    /**
     * 泛型变量的解析器
     */
    private var variableResolver: VariableResolver? = null

    /**
     * TypeProvider
     */
    private var typeProvider: TypeProvider? = null

    /**
     * 泛型列表
     */
    private var generics: Array<ResolvableType>? = null

    /**
     * 接口列表
     */
    private var interfaces: Array<ResolvableType>? = null

    /**
     * 父类
     */
    private var superType: ResolvableType? = null

    private constructor(clazz: Class<*>?) {
        this.resolved = clazz ?: Any::class.java
        this.type = resolved
        this.typeProvider = null
        this.variableResolver = null
        this.componentType = null
    }

    private constructor(
        type: Type?,
        typeProvider: TypeProvider?,
        variableResolver: VariableResolver?,
        componentType: ResolvableType?
    ) {
        this.type = type
        this.typeProvider = typeProvider
        this.variableResolver = variableResolver
        this.resolved = resolveClass()
        this.componentType = componentType
    }

    private constructor(
        type: Type?,
        typeProvider: TypeProvider?,
        variableResolver: VariableResolver?
    ) {
        this.type = type
        this.typeProvider = typeProvider
        this.variableResolver = variableResolver
        this.resolved = resolveClass()
        this.componentType = null
    }

    /**
     * 根据type, 去执行对于Class的解析
     *
     * @return 解析得到的Class
     */
    private fun resolveClass(): Class<*>? {
        // 如果为EmptyType, 那么return null
        if (this.type == EmptyType.INSTANCE) {
            return null
        }
        // 如果type不存在, 那么直接return null, 无法解析出来合适的Class
        this.type ?: return null

        if (this.type is Class<*>) {
            return this.type as Class<*>
        }
        // 泛型数组, 比如E[], T[]
        if (this.type is GenericArrayType) {
            // 获取到元素类型E, 并完成ComponentType的解析...
            val resolvedComponent = getComponentType().resolve() ?: return null

            // 根据元素类型去创建数组, 再去getClass...
            return java.lang.reflect.Array.newInstance(resolvedComponent, 0)::class.java
        }
        // 非泛型数组
        return resolveType().resolve()
    }

    /**
     * 获取当前类型对应的原始的JavaClass(比如List<String>, 将会返回List), 如果无法获取的话, return null
     *
     * @return Raw JavaClass
     */
    open fun getRawClass(): Class<*>? {
        if (this.resolved == this.type) {
            return this.resolved
        }
        var rawType = this.type
        if (rawType is ParameterizedType) {
            rawType = rawType.rawType
        }
        return if (rawType is Class<*>) rawType else null
    }

    /**
     * 返回解析完成得到的Class, 如果没有已经完成解析的Class, 那么返回Object.class
     *
     * @return 已经完成解析的Class(或者Object.class)
     */
    open fun toClass(): Class<*> {
        return resolve(Any::class.java)
    }

    /**
     * 返回解析得到的Class, 如果没有已经解析完成的Class, 那么返回给定的fallback的Class
     *
     * @return 已经完成解析的Class(或者给定的fallback的Class)
     */
    open fun resolve(fallback: Class<*>): Class<*> {
        return this.resolved ?: fallback
    }

    /**
     * 根据给定的indexes去返回对应的泛型参数的ResolvableType,
     * 例如Map<Integer, Map<String, Double>>, getGeneric(0)将会得到Integer,
     * getGeneric(1,0)将会得到String, getGeneric(1,1)将会得到Double.
     *
     * 如果没有给定indexes, 那么将会返回第一个泛型参数的ResolvableType
     *
     * @param indexes indexes
     * @return 泛型参数的ResolvableType(找不到的话, 返回NONE)
     */
    open fun getGeneric(vararg indexes: Int): ResolvableType {
        var generics = getGenerics()
        if (indexes.isEmpty()) {
            return if (generics.isEmpty()) NONE else generics[0]
        }

        var generic: ResolvableType = this
        for (index in indexes) {
            generics = generic.getGenerics()
            if (index < 0 || index >= generics.size) {
                return NONE
            }
            generic = generics[index]
        }
        return generic
    }

    /**
     * 获取该类型对应的泛型列表
     *
     * @return 泛型列表
     */
    open fun getGenerics(): Array<ResolvableType> {
        if (this == NONE) {
            return EMPTY_TYPES_ARRAY
        }
        var generics = this.generics
        if (generics == null) {
            if (this.type is Class<*>) {

                // 如果当前类是一个抽象类, 那么通过typeParameters, 可以获取到抽象类定义的泛型
                // 例如List<E>, 在这里typeParameters可以拿到E, 元素类型为TypeVariable
                val typeParameters = (type as Class<*>).typeParameters
                generics = Array(typeParameters.size) { forType(typeParameters[it], this) }

                // ParameterizedType->带泛型的类型, 例如List<String>, List<E>
                // 如果是List<String>的情况, 这里actualTypeArguments可以拿到[String.class]
                // 如果是List<E>的情况, 这里actualTypeArguments可以拿到[E], 这里的E的类型是Class
            } else if (this.type is ParameterizedType) {
                val actualTypeArguments = (type as ParameterizedType).actualTypeArguments
                generics = Array(actualTypeArguments.size) { forType(actualTypeArguments[it], this) }
            } else {
                generics = resolveType().getGenerics()
            }
            this.generics = generics
        }
        return generics
    }

    open fun resolveGenerics(): Array<Class<*>?> {
        val generics = getGenerics()
        return Array(generics.size) { generics[it].resolve() }
    }

    open fun resolveGeneric(vararg indexes: Int): Class<*>? {
        return getGeneric(*indexes).resolve()
    }

    open fun hasGenerics(): Boolean {
        return getGenerics().isNotEmpty()
    }

    open fun asCollection(): ResolvableType {
        return `as`(Collection::class.java)
    }

    open fun asMap(): ResolvableType {
        return `as`(Map::class.java)
    }

    open fun getComponentType(): ResolvableType {
        if (this == NONE) {
            return NONE
        }
        if (this.componentType != null) {
            return this.componentType!!
        }
        if (this.type is Class<*>) {
            val componentType = (this.type as Class<*>).componentType
            return forType(componentType, this.variableResolver)
        }
        if (this.type is GenericArrayType) {
            return forType((this.type as GenericArrayType).genericComponentType, this.variableResolver)
        }
        return resolveType().getComponentType()
    }

    open fun isArray(): Boolean {
        if (this == NONE) {
            return false
        }
        if (this.type is Class<*>) {
            return (this.type as Class<*>).isArray
        }
        if (this.type is GenericArrayType) {
            return true
        }
        return resolveType().isArray()
    }

    open fun `as`(type: Class<*>): ResolvableType {
        if (this == NONE) {
            return NONE
        }
        val resolved = resolve()
        if (resolved == null || resolved == type) {
            return this
        }
        // 遍历当前类的所有直接接口
        for (interfaceType in getInterfaces()) {

            // 使用该接口, 递归去执行as方法
            val interfaceAsType = interfaceType.`as`(type)
            if (interfaceAsType != NONE) {
                return interfaceAsType
            }
        }
        return getSuperType().`as`(type)
    }

    open fun getInterfaces(): Array<ResolvableType> {
        val resolved = resolve() ?: return EMPTY_TYPES_ARRAY
        var interfaces = this.interfaces
        if (this.interfaces == null) {
            val genericInterfaces = resolved.genericInterfaces

            // 为所有的泛型接口, 去构建对应的ResolvableType
            // 每个泛型接口的owner都是子类(子接口), type指定父接口
            interfaces = Array(genericInterfaces.size) { forType(genericInterfaces[it], this) }
            this.interfaces = interfaces
        }
        return interfaces!!
    }

    open fun getSuperType(): ResolvableType {
        val resolved = resolve() ?: return NONE
        val superclass = resolved.genericSuperclass ?: return NONE

        var superType = superType
        if (superType == null) {
            // 为superType, 去构建ResolvableType
            // 每个泛型父类的owner都是子类, type=superClass
            superType = forType(superclass, this)
            this.superType = superType
        }
        return superType
    }

    /**
     * 返回解析得到的Class, 如果没有已经完成解析的Class, 那么return null
     *
     * @return 已经完成解析的Class(如果不存在的话, return null)
     */
    open fun resolve(): Class<*>? {
        return this.resolved
    }

    /**
     * 指定对于type的解析
     *
     * @return 对type执行解析得到的ResolvableType
     */
    private fun resolveType(): ResolvableType {
        // 如果是ParameterizedType, 例如List<String>, Map<String, String>
        if (this.type is ParameterizedType) {
            return forType((type as ParameterizedType).rawType, this.variableResolver)
        }
        // 如果是WildcardType, 通配符的情况, 也就是"? extends"/"? super"这种情况
        if (this.type is WildcardType) {
            var resolved = resolveBounds((type as WildcardType).upperBounds)
            if (resolved == null) {
                resolved = resolveBounds((type as WildcardType).lowerBounds)
            }
            return forType(resolved, this.variableResolver)
        }

        // 如果是TypeVariable, 说明是一个泛型的元素, 例如List<E>中的E就是TypeVariable
        // 这种情况, 我们就得使用variableResolver去进行解析...
        // 例如对于ArrayList<String>的接口列表可以获取到List<E>, List<E>的接口列表可以获取到Collection<E>
        // 当使用asCollection方法获取到Collection<E>时, 就得尝试从子类找到List<E>
        // List<E>再往子类去找到ArrayList<String>, 通过泛型名字E, 从而最终找到元素类型String
        if (this.type is TypeVariable<*>) {
            val typeVariable = this.type as TypeVariable<*>
            val resolved = this.variableResolver?.resolveVariable(typeVariable)
            if (resolved != null) {
                return resolved
            }
            return forType(resolveBounds(typeVariable.bounds), this.variableResolver)
        }
        return NONE
    }

    private fun resolveBounds(bounds: Array<Type>): Type? {
        if (bounds.isEmpty() || bounds[0] == Any::class.java) {
            return null
        }
        return bounds[0]
    }


    /**
     * 执行对于泛型变量的解析
     *
     * @param typeVariable 带解析的泛型变量, 比如E/T/K,V
     */
    private fun resolveVariable(typeVariable: TypeVariable<*>): ResolvableType? {

        if (this.type is ParameterizedType) {
            val parameterizedType = this.type as ParameterizedType
            val resolved = resolve() ?: return null
            val variables = resolved.typeParameters

            // 遍历当前类定义的所有的泛型参数, 去寻找, 如果该泛型参数的name和给定的TypeVariable的name一致
            // 那么就去返回该位置的泛型参数...比如ArrayList<String>, 对于Collection<E>, 就能从子类当中
            // 去解析得到ArrayList当中对应位置为E的泛型, 从而解析得到String...
            for ((index, variable) in variables.withIndex()) {
                if (variable.name == typeVariable.name) {
                    val actualType = parameterizedType.actualTypeArguments[index]
                    return forType(actualType, this.variableResolver)
                }
            }

            val ownerType = parameterizedType.ownerType
            if (ownerType != null) {
                return forType(ownerType, this.variableResolver).resolveVariable(typeVariable)
            }
        }

        return null
    }

    /**
     * 将当前ResolvableType转换为泛型变量的解析器
     *
     * @return 泛型变量解析器
     */
    open fun asVariableResolver(): VariableResolver? {
        if (this == NONE) {
            return null
        }
        return DefaultVariableResolver(this)
    }

    open fun isInstance(obj: Any?): Boolean {
        return obj != null && isAssignableFrom(obj::class.java)
    }

    open fun isAssignableFrom(other: Class<*>?): Boolean {
        return isAssignableFrom(forClass(other), null)
    }

    open fun isAssignableFrom(owner: ResolvableType): Boolean {
        return isAssignableFrom(owner, null)
    }

    private fun isAssignableFrom(other: ResolvableType, matchedBefore: Map<Type, Type>?): Boolean {
        if (this == NONE || other == NONE) {
            return false
        }
        if (isArray()) {
            return this.getComponentType().isAssignableFrom(other.getComponentType())
        }
        // 先这么判断吧, 后面再修一下...
        return resolve()!!.isAssignableFrom(other.resolve()!!)
    }

    override fun toString(): String {
        if (isArray()) {
            return getComponentType().toString() + "[]"
        }
        if (this.resolved == null) {
            return "?"
        }
        if (this.type is TypeVariable<*>) {
            if (variableResolver?.resolveVariable(this.type as TypeVariable<*>) == null) {
                return "?"
            }
        }
        if (this.hasGenerics()) {
            return resolved!!.name + "<" + getGenerics().joinToString(",") { it.toString() } + ">"
        }
        return this.resolved!!.name
    }


    companion object {
        /**
         * 空的ResolvableType常量
         */
        @JvmField
        val NONE = ResolvableType(EmptyType.INSTANCE, null, null)

        /**
         * 空的ResolvableType[]常量
         */
        @JvmField
        val EMPTY_TYPES_ARRAY = emptyArray<ResolvableType>()

        @JvmStatic
        fun forClass(clazz: Class<*>?): ResolvableType {
            return ResolvableType(clazz)
        }

        @JvmStatic
        fun forField(field: Field): ResolvableType {
            return forType(null, FieldTypeProvider(field), null)
        }

        @JvmStatic
        fun forType(type: Type?): ResolvableType {
            return forType(type, null, null)
        }

        @JvmStatic
        fun forType(type: Type?, owner: ResolvableType?): ResolvableType {
            return forType(type, owner?.asVariableResolver())
        }

        @JvmStatic
        fun forType(
            type: Type?,
            variableResolver: VariableResolver?
        ): ResolvableType {
            return forType(type, null, variableResolver)
        }

        @JvmStatic
        private fun forType(
            type: Type?,
            typeProvider: TypeProvider?,
            variableResolver: VariableResolver?
        ): ResolvableType {
            var typeToUse = type
            if (typeToUse == null && typeProvider != null) {
                typeToUse = typeProvider.getType()
            }
            if (typeToUse == null) {
                return NONE
            }

            if (typeToUse is Class<*>) {
                return ResolvableType(typeToUse, typeProvider, variableResolver, null)
            }

            return ResolvableType(typeToUse, typeProvider, variableResolver)
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

        @JvmStatic
        fun clearCache() {

        }
    }

    /**
     * TypeProvider
     */
    interface TypeProvider {
        fun getType(): Type?

        fun getSource(): Any? = null
    }

    private class FieldTypeProvider(private val field: Field) : TypeProvider {
        override fun getType(): Type? {
            return field.genericType
        }

        override fun getSource(): Any {
            return field
        }
    }


    /**
     * 提供对于TypeVariable的解析的Resolver策略接口
     *
     * @see TypeVariable
     */
    interface VariableResolver {

        /**
         * 返回VariableResolver的source
         *
         * @return source of VariableResolver
         */
        fun getSource(): Any

        /**
         * 执行对于泛型变量的解析
         *
         * @param typeVariable 泛型变量
         * @return 解析完成得到的变量值, 解析不到的话return null
         */
        fun resolveVariable(typeVariable: TypeVariable<*>): ResolvableType?
    }

    private class DefaultVariableResolver(private val source: ResolvableType) : VariableResolver {
        override fun getSource(): Any {
            return source
        }

        override fun resolveVariable(typeVariable: TypeVariable<*>): ResolvableType? {
            return source.resolveVariable(typeVariable)
        }
    }

    /**
     * 空的Type标识
     */
    private class EmptyType : Type, Serializable {
        companion object {
            @JvmField
            val INSTANCE = EmptyType()
        }

        fun readResolve(): Any = INSTANCE
    }
}