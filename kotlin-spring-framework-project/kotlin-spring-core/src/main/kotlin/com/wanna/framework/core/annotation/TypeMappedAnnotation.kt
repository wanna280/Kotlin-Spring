package com.wanna.framework.core.annotation

import com.wanna.framework.constants.CLASS_ARRAY_TYPE
import com.wanna.framework.constants.STRING_ARRAY_TYPE
import com.wanna.framework.core.annotation.MergedAnnotation.Adapt
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import java.lang.reflect.Method
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

/**
 * TypeMapped MergedAnnotation
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 *
 * @param mapping 注解的映射信息
 * @param classLoader ClassLoader
 * @param source source
 * @param rootAttributes root属性(可能是Map, 可能是Annotation)
 * @param valueExtractor 从root属性当中去提取到对应的属性值的提取器Extractor
 * @param useMergedValues 是否要使用MergedValues? 决定了@AliasFor注解是否生效
 * @param attributeFilter 属性值的过滤器, 提供对于属性名的过滤, 被该Filter匹配上的属性名将会被丢弃
 */
open class TypeMappedAnnotation<A : Annotation>(
    private val mapping: AnnotationTypeMapping,
    @Nullable private val classLoader: ClassLoader?,
    @Nullable private val source: Any?,
    @Nullable private val rootAttributes: Any?,
    private val valueExtractor: ValueExtractor,
    private val useMergedValues: Boolean = true,
    @Nullable private val attributeFilter: Predicate<String>? = null
) : AbstractMergedAnnotation<A>() {

    /**
     * root注解的Mirrors
     */
    private val resolvedRootMirrors = mapping.root.mirrorSets.resolve(source, rootAttributes, valueExtractor)

    /**
     * 当前注解的Mirrors
     */
    private val resolvedMirrors: IntArray = if (distance == 0) resolvedRootMirrors
    else mapping.mirrorSets.resolve(source, rootAttributes, this::getValueForMirrorResolution)


    /**
     * present?
     */
    override val present: Boolean
        get() = true

    /**
     * distance, 也就是当前注解距离root注解的距离
     */
    final override val distance: Int
        get() = mapping.distance

    /**
     * AnnotationType
     */
    @Suppress("UNCHECKED_CAST")
    override val type: Class<A>
        get() = this.mapping.annotationType as Class<A>

    /**
     * root MergedAnnotation
     */
    override val root: MergedAnnotation<*>
        get() = if (distance == 0) this else TypeMappedAnnotation<Annotation>(
            mapping.root, classLoader, source, rootAttributes, valueExtractor
        )

    override fun hasDefaultValue(attributeName: String): Boolean {
        val attributeIndex = getAttributeIndex(attributeName, true)
        val value = getValue(attributeIndex, true, false)
        return value == null
    }

    override fun <T : Any> getDefaultValue(attributeName: String, type: Class<T>): Optional<T> {
        val attributeIndex = getAttributeIndex(attributeName, false)
        // 如果不存在该属性名对应的属性方法, return empty
        if (attributeIndex == -1) {
            return Optional.empty()
        }
        // 如果存在的话, 返回该属性方法的默认值
        val attribute = this.mapping.attributes[attributeIndex]
        return Optional.ofNullable(adapt(attribute, attribute.defaultValue, type))
    }

    @Nullable
    override fun <T> getAttributeValue(attributeName: String, type: Class<T>): T? {
        // 先去获取该属性对应的index
        val attributeIndex = getAttributeIndex(attributeName, false)

        // 如果index=-1, 代表不存在return null; 如果index!=-1, 那么根据index去获取到对应的属性值
        return if (attributeIndex == -1) null else getValue(attributeIndex, type)
    }

    /**
     * 对MergedAnnotation当中的属性值去进行过滤, 得到一个新的MergedAnnotation
     *
     * @param predicate 对属性名去执行匹配的断言, 如果断言和属性名匹配了, 那么该属性将会被pass掉
     * @return 只用于符合断言的属性名的MergedAnnotation
     */
    override fun filterAttributes(predicate: Predicate<String>): MergedAnnotation<A> {
        // 如果之前就有AttributeFilter, 去进行merge
        val predicateToUse = if (this.attributeFilter != null) predicate.and(this.attributeFilter) else predicate
        return TypeMappedAnnotation<A>(
            mapping, classLoader, source, rootAttributes, valueExtractor, useMergedValues, predicateToUse
        )
    }

    /**
     * 获取到不使用Merged的原始注解的属性的MergedAnnotation(对于@AliasFor将不会生效)
     *
     * @return 不含有Merged属性的MergedAnnotation
     */
    override fun withNonMergedAttributes(): MergedAnnotation<A> {
        return TypeMappedAnnotation(mapping, classLoader, source, rootAttributes, valueExtractor, false)
    }

    /**
     * 对于转换为Map, 我们使用LinkedHashMap去进行创建
     *
     * @param adapts 转换时需要用到的操作(是否需要将Class转String/是否需要将Annotation转Map)
     * @return 转换之后得到的Map
     */
    override fun asMap(vararg adapts: Adapt): Map<String, Any> {
        return Collections.unmodifiableMap(asMap(Function { LinkedHashMap() }, adapts = adapts))
    }

    /**
     * 执行真正的Map的转换, 将当前的MergedAnnotation去转换成为一个Map
     *
     * @param factory 创建Map的工厂方法
     * @param adapts 转换时需要用到的操作(是否需要将Class转String/是否需要将Annotation转Map)
     * @return 转换成功之后得到的目标Map
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Map<String, Any>> asMap(
        factory: Function<MergedAnnotation<A>, T>, vararg adapts: Adapt
    ): T {
        // apply func, 获取到Map对象
        val map = factory.apply(this)
        if (!ClassUtils.isAssignableValue(MutableMap::class.java, map)) {
            throw IllegalStateException("Factory used to create MergedAnnotation Map must a mutable map")
        }
        map as MutableMap<String, Any>


        // 针对当前注解当中的所有的属性方法, 去获取到属性值
        val attributes = this.mapping.attributes
        for (index in 0 until attributes.size) {
            val attribute = attributes[index]

            // 如果需要去执行classToString, 并且目标属性类型是Class/Class[]的话, 那么需要转换为String/String[]
            val value = if (isFiltered(attribute.name)) null
            else getValue(index, getTypeForMapOptions(attribute, adapts = adapts))
            if (value != null) {
                // 对该属性值去类型的转换, 并收集到map当中来
                val adapted = adaptValueForMapOptions(attribute, value, map.javaClass, factory, arrayOf(*adapts))
                map[attribute.name] = adapted
            }
        }
        return map
    }

    /**
     * 为属性值转换成为Map的方式, 去获取到应该Value应该使用的属性值类型
     *
     * @param attribute 目标注解属性方法
     * @param adapts 转换时需要用到的操作(是否需要将Class转String/是否需要将Annotation转Map)
     * @return 如果需要去执行classToString, 并且目标属性类型是Class/Class[]的话, 那么需要转换为String/String[]
     */
    private fun getTypeForMapOptions(attribute: Method, vararg adapts: Adapt): Class<*> {
        val attributeType: Class<*> = attribute.returnType
        // 获取到目标方法的属性的componentType
        val componentType = attributeType.componentType ?: attributeType

        // 如果注解当中的属性对应元素类型是Class, 并且需要classToString的话, 那么需要返回String/String[]
        if (Adapt.CLASS_TO_STRING.isIn(*adapts) && componentType === Class::class.java) {
            return if (attributeType.isArray) STRING_ARRAY_TYPE else String::class.java
        }
        // 返回Object, 交给后面的方法去进行自动类型推断
        return Any::class.java
    }

    /**
     * 将value去转换成为Map需要的目标类型, 这里主要针对MergedAnnotation/MergedAnnotation[]的情况, 检查是否需要转换成为Map...
     *
     * @param attribute 目标属性方法
     * @param value 目标属性值
     * @param mapType mapType
     * @param factory 将MergedAnnotation转换为Map的Function
     * @param adapts 转换时需要用到的操作(是否需要将Class转String/是否需要将Annotation转Map)
     * @return 转换之后得到的结果
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Map<String, Any>, A : Annotation> adaptValueForMapOptions(
        attribute: Method,
        value: Any,
        mapType: Class<*>,
        factory: Function<MergedAnnotation<A>, T>,
        adapts: Array<Adapt>
    ): Any {

        // 如果原始的Value是一个MergedAnnotation的话, 那么需要检查是否需要转换成为一个Map
        if (value is MergedAnnotation<*>) {
            return if (!Adapt.ANNOTATION_TO_MAP.isIn(*adapts)) value.synthesize()
            else (value as MergedAnnotation<A>).asMap(factory, *adapts)

            // 如果原始的Value是一个MergedAnnotation[]的话, 那么需要检查是否需要转换成为Map
        } else if (value is Array<*> && value.isArrayOf<MergedAnnotation<A>>()) {
            val result: Any
            // 如果需要annotationToMap, 那么针对每个注解都去转换成为Map
            if (Adapt.ANNOTATION_TO_MAP.isIn(*adapts)) {
                result = java.lang.reflect.Array.newInstance(mapType, value.size)
                for (index in 0 until value.size) {
                    java.lang.reflect.Array.set(
                        result, index, (value[index] as MergedAnnotation<A>).asMap(factory, *adapts)
                    )
                }
                // 如果不需要annotationToMap的话, 那么针对每个MergedAnnotation去进行注解的合成...
            } else {
                result = java.lang.reflect.Array.newInstance(attribute.returnType.componentType, value.size)
                for (index in 0 until value.size) {
                    java.lang.reflect.Array.set(result, index, (value[index] as MergedAnnotation<A>).synthesize())
                }
            }
            return result
        }
        // 如果不是MergedAnnotation/MergedAnnotation[]的话, 在之前已经做好类型转换了, 这里就不管了...
        return value
    }

    /**
     * 针对Mirror解析的情况下, 去获取到Value
     *
     * @param attribute 目标属性方法
     * @param annotation annotation
     * @return 解析到的Value
     */
    @Nullable
    private fun getValueForMirrorResolution(attribute: Method, annotation: Any?): Any? {
        val index = this.mapping.attributes.indexOf(attribute)
        val valueAttribute = MergedAnnotation.VALUE == attribute.name
        return getValue(index, !valueAttribute, true)
    }

    /**
     * 获取给定的属性名方法, 去获取到该方法对应的AttributeIndex
     *
     * @param name 属性名name(对应的其实就是一个注解的方法的方法名)
     * @param required 该属性是否是必须的?
     * @return 存在的话return AttributeIndex; 如果不存在并且required=false的话, return -1
     * @throws NoSuchElementException 如果不存在有这样的属性名方法, 并且require=true
     */
    private fun getAttributeIndex(name: String, required: Boolean): Int {
        // 检查该注解是否存在有这样的名字的属性方法?
        val index = if (isFiltered(name)) -1 else this.mapping.attributes.indexOf(name)

        // 如果不存在有这样的属性名方法, 但是require=true的话, 需要丢出来异常...
        if (index == -1 && required) {
            throw NoSuchElementException("No attribute method '$name' present in merged annotation ${type.name}")
        }
        return index
    }

    /**
     * 根据AttributeIndex去获取到对应的属性值
     *
     * @param attributeIndex attributeIndex
     * @param type 要将该属性值去转换成为什么类型
     * @return 转换之后的属性值
     */
    @Nullable
    private fun <T> getValue(attributeIndex: Int, type: Class<T>): T? {
        // 根据AttributeIndex, 去获取到该位置的注解属性方法
        val attribute = this.mapping.attributes[attributeIndex]

        // 从该注解的属性方法当中, 根据AttributeIndex去获取到对应的属性值作为value
        // 如果获取不到的话, 使用该属性方法的默认值去作为value...
        val value = getValue(attributeIndex, true, false) ?: attribute.defaultValue

        // 完成类型转换
        return adapt(attribute, value, type)
    }

    /**
     * 根据AttributeIndex, 去获取到对应位置的属性值
     *
     * @param attributeIndex attributeIndex
     * @param useConventionMapping 是否需要使用ConventionMapping?
     * @param forMirrorResolution 是否被用作Mirror的解析?
     */
    @Nullable
    private fun getValue(attributeIndex: Int, useConventionMapping: Boolean, forMirrorResolution: Boolean): Any? {
        var mapping = this.mapping
        var attrIndex = attributeIndex

        // 如果要使用mergedValues的话, 那么需要尝试使用root注解的相关属性
        if (useMergedValues) {

            // 尝试检查root注解当中是否存在有@AliasFor注解去指向当前正在处理的属性?
            var mappedIndex = mapping.getAliasMapping(attributeIndex)
            if (mappedIndex == -1 && useConventionMapping) {
                mappedIndex = mapping.getConventionMapping(attributeIndex)
            }

            // 如果mappedIndex不为-1的话, 代表需要使用root注解的相关属性信息去进行返回, 将index&mapping都设置为对应的root
            if (mappedIndex != -1) {
                mapping = mapping.root
                attrIndex = mappedIndex
            }
        }

        // 如果不是作为Mirror的解析的情况的话, 那么我们还需要从mirrors当中去获取到对应的属性的index
        // 这种情况, 主要是用来处理比如A注解内部有name/value这两个属性, 但是它们互相指向的情况
        // 只走上面的AliasMapping只能获取到它的source当中的@AliasFor, 但是无法处理一个注解内部的两个属性互相@AliasFor的情况
        if (!forMirrorResolution) {
            attrIndex = (if (mapping.distance == 0) resolvedRootMirrors else resolvedMirrors)[attrIndex]
        }

        if (attrIndex == -1) {
            return null
        }

        // 如果mapping对应的distance=0的话, 说明是需要处理的是root注解, 直接使用valueExtractor去进行提取即可
        if (mapping.distance == 0) {
            val attribute = mapping.attributes[attrIndex]
            val value = this.valueExtractor.extract(attribute, this.rootAttributes)
            return value ?: attribute.defaultValue
        }
        // 如果mapping的distance不是0的话, 那么从Meta注解当中去获取属性值
        return getValueFromMetaAnnotation(attributeIndex, forMirrorResolution)
    }

    /**
     * 从Meta注解当中, 去获取到对应的属性值
     *
     * @param attributeIndex attributeIndex
     * @param forMirrorResolution 是否被用作Mirror的解析的情况?
     * @return 根据AttributeIndex, 去获取到的属性值
     */
    @Nullable
    private fun getValueFromMetaAnnotation(attributeIndex: Int, forMirrorResolution: Boolean): Any? {
        var value: Any? = null
        if (useMergedValues || forMirrorResolution) {
            value = this.mapping.getMappedAnnotationValue(attributeIndex, forMirrorResolution)
        }

        // 反射执行目标属性方法...去获取到对应的属性值...
        if (value == null) {
            val attribute = this.mapping.attributes[attributeIndex]
            value = ReflectionUtils.invokeMethod(attribute, this.mapping.annotation)
        }
        return value
    }

    /**
     * 根据给定的对象类型去获取ValueExtractor
     *
     * @return ValueExtractor
     */
    private fun getValueExtractor(@Nullable value: Any?): ValueExtractor {
        if (value is Annotation) {
            return ValueExtractor { method, obj -> ReflectionUtils.invokeMethod(method, obj) }
        }
        if (value is Map<*, *>) {
            return ValueExtractor { method, obj -> extractFromMap(method, obj) }
        }
        return this.valueExtractor
    }

    /**
     * 检查给定的属性名是否因为被AttributeFilter所过滤, 从而不需要?
     *
     * @return 如果被AttributeFilter所匹配上了, 那么该属性值将会被过滤掉
     */
    private fun isFiltered(name: String): Boolean = this.attributeFilter?.test(name) ?: false


    /**
     * 将给定的属性值, 去转换成为目标类型的对象
     */
    @Suppress("UNCHECKED_CAST")
    @Nullable
    private fun <T> adapt(attribute: Method, @Nullable value: Any?, type: Class<T>): T? {
        value ?: return null
        var result: Any? = adaptForAttribute(attribute, value)

        // 如果type=Object的话, 对type去进行转换, 使用注解的属性类型作为targetType
        val targetType = getAdaptType(attribute, type)

        // Class->String
        if (value is Class<*> && targetType == String::class.java) {
            result = value.name

            // String->Class
        } else if (value is String && targetType == Class::class.java) {
            result = ClassUtils.resolveClassName(value, getClassLoader())

            // Class[]->String[]
        } else if (value is Array<*> && value.isArrayOf<Class<*>>() && targetType == STRING_ARRAY_TYPE) {
            // 为目标类型的componentType去创建数组, 并将value当中的元素copy&getName放入到result当中
            result = java.lang.reflect.Array.newInstance(targetType.componentType, value.size)
            for (index in value.indices) {
                java.lang.reflect.Array.set(result, index, (value[index] as Class<*>).name)
            }

            // String[]->Class[]
        } else if (value is Array<*> && value.isArrayOf<String>() && targetType == CLASS_ARRAY_TYPE) {
            // 为目标类型的componentType去创建数组, 并将value当中的元素copy并完成类加载并放入到result当中
            result = java.lang.reflect.Array.newInstance(targetType.componentType, value.size)
            for (index in value.indices) {
                java.lang.reflect.Array.set(
                    result, index, ClassUtils.resolveClassName(value[index] as String, getClassLoader())
                )
            }

            // MergedAnnotation->Annotation(因为MetadataReader读取出来的注解会是MergedAnnotation对象)
        } else if (value is MergedAnnotation<*> && targetType.isAnnotation) {
            result = value.synthesize()

            // MergedAnnotation[]->Annotation[](因为MetadataReader读取出来的注解会是MergedAnnotation对象)
        } else if (value is Array<*> && value.isArrayOf<MergedAnnotation<*>>() && targetType.isArray && targetType.componentType.isAnnotation) {
            // 为目标类型的componentType去创建数组, 并将value当中的元素copy&synthesize放入到result当中
            result = java.lang.reflect.Array.newInstance(targetType.componentType, value.size)
            for (index in value.indices) {
                java.lang.reflect.Array.set(result, index, (value[index] as MergedAnnotation<*>).synthesize())
            }
        }

        // 如果类型不匹配的话...那么丢出异常(Note: 这里要求type必须是包装类的类型)
        if (!targetType.isInstance(result)) {
            throw IllegalArgumentException("Unable to adapt value of type ${result!!.javaClass.name} to ${targetType.name}")
        }
        return result as T?
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getAdaptType(attribute: Method, type: Class<T>): Class<T> {
        if (type != Any::class.java) {
            return type
        }
        val attributeType = attribute.returnType
        if (attributeType.isAnnotation) {
            return MergedAnnotation::class.java as Class<T>
        }
        if (attributeType.isAnnotation && attributeType.componentType.isAnnotation) {
            return emptyArray<MergedAnnotation<*>>()::class.java as Class<T>
        }

        // 如果是八种基础数据类型的话, 那么我们使用包装类去进行返回
        return ClassUtils.resolvePrimitiveIfNecessary(type) as Class<T>
    }

    private fun adaptForAttribute(attribute: Method, value: Any): Any {
        val attributeType = ClassUtils.resolvePrimitiveIfNecessary(attribute.returnType)
        // TODO
        return value
    }

    /**
     * 获取ClassLoader
     *
     * @return ClassLoader
     */
    @Nullable
    private fun getClassLoader(): ClassLoader? = this.classLoader

    /**
     * 执行注解的合成
     *
     * @return 合成的注解对象
     */
    @Suppress("UNCHECKED_CAST")
    override fun createSynthesized(): A {
        if (type.isInstance(rootAttributes) && !isSynthesizable()) {
            return rootAttributes as A
        }
        return SynthesizedMergedAnnotationInvocationHandler.createProxy(this, type)
    }

    /**
     * 检查是否可以去合成?
     *
     * @return 如果需要去进行合成return true; 否则return false
     */
    private fun isSynthesizable(): Boolean {
        // 如果已经合成过了, 那么return false
        if (this.rootAttributes is SynthesizedAnnotation) {
            return false
        }

        // 如果还没合成过, 那么需要检查@AliasFor注解, 去判断是否需要去进行合成
        return this.mapping.synthesizable
    }

    companion object {

        @Nullable
        @JvmStatic
        fun <A : Annotation> createIfPossible(
            mapping: AnnotationTypeMapping, annotation: MergedAnnotation<*>
        ): MergedAnnotation<A>? {
            if (annotation is TypeMappedAnnotation<*>) {
                return createIfPossible(
                    mapping, annotation.source, annotation.rootAttributes, 0, annotation.valueExtractor
                )
            }
            return null
        }

        @Nullable
        @JvmStatic
        fun <A : Annotation> createIfPossible(
            mapping: AnnotationTypeMapping, source: Any?, annotation: Annotation?, aggregateIndex: Int
        ): MergedAnnotation<A>? {
            return createIfPossible(mapping, source, annotation, aggregateIndex, ReflectionUtils::invokeMethod)
        }

        @Nullable
        @JvmStatic
        fun <A : Annotation> createIfPossible(
            mapping: AnnotationTypeMapping,
            source: Any?,
            rootAnnotation: Any?,
            aggregateIndex: Int,
            valueExtractor: ValueExtractor,
        ): MergedAnnotation<A>? {
            return TypeMappedAnnotation(mapping, null, source, rootAnnotation, valueExtractor)
        }

        /**
         * 根据注解的Attributes信息的Map 去快速构建MergedAnnotation的工厂方法
         *
         * @param classLoader ClassLoader
         * @param source source
         * @param attributes attributes(注解的属性信息)
         * @return MergedAnnotation
         */
        @JvmStatic
        fun <A : Annotation> of(
            mapping: AnnotationTypeMapping,
            @Nullable classLoader: ClassLoader?,
            @Nullable source: Any?,
            attributes: Map<String, Any>,
            valueExtractor: ValueExtractor
        ): MergedAnnotation<A> {
            return TypeMappedAnnotation(mapping, classLoader, source, attributes, valueExtractor)
        }

        /**
         * 根据给定的注解, 去构建出来MergedAnnotation
         *
         * @param A 注解类型
         * @param annotation Annotation
         * @return MergedAnnotation for given Annotation
         */
        @JvmStatic
        fun <A : Annotation> from(annotation: A): MergedAnnotation<A> = from(null, annotation)

        /**
         * 根据给定的注解, 去构建出来MergedAnnotation
         *
         * @param A 注解类型
         * @param source source
         * @param annotation Annotation
         * @return MergedAnnotation for given Annotation
         */
        @JvmStatic
        fun <A : Annotation> from(@Nullable source: Any?, annotation: A): MergedAnnotation<A> {
            // 为该注解去构建出来注解的Meta注解的映射信息
            val mappings = AnnotationTypeMappings.forAnnotationType(annotation.annotationClass.java)
            return TypeMappedAnnotation(mappings[0], null, source, annotation, ReflectionUtils::invokeMethod)
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun extractFromMap(method: Method, map: Any?): Any? {
            return if (map == null) null else (map as Map<String, Any>)[method.name]
        }
    }
}