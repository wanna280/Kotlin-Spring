package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.util.StringUtils
import java.lang.reflect.Method
import java.util.Arrays

/**
 * 针对一个注解, 建立起来的映射关系
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
class AnnotationTypeMapping(
    @Nullable private val source: AnnotationTypeMapping?,
    val annotationType: Class<out Annotation>,
    val annotation: Annotation?
) {
    /**
     * 当前注解距离root注解的深度
     */
    var distance: Int = 0

    /**
     * root注解的Mapping映射信息
     */
    val root: AnnotationTypeMapping

    /**
     * 当前注解当中的属性方法
     */
    val attributes: AttributeMethods

    /**
     * alias别名的映射关系, index->当前注解当中的属性方法的index, value->root注解当中通过@AliasFor去指向了该属性方法的root属性方法的位置index
     */
    private val aliasMappings: IntArray

    /**
     * convention映射关系
     */
    private val conventionMappings: IntArray

    private val annotationValueMappings: IntArray

    private val annotationValueSource: Array<AnnotationTypeMapping?>

    /**
     * aliasedBy, Key是@AliasFor注解指向的目标注解属性方法, Value是标注了当前注解当中的@AliasFor注解的属性方法;
     * 为什么要这样设计呢? 比如@Configuration注解的value上标注了@AliasFor注解, 并且指向了@Component的value属性,
     * 那么在使用时, 其实我们是需要使用@Component的value属性去寻找@Configuration的value属性的, 因此建立的映射关系是被动关系.
     */
    private val aliasedBy: Map<Method, List<Method>>

    /**
     * MirrorSets
     */
    val mirrorSets: MirrorSets

    /**
     * 声明的Alias的属性方法
     */
    private val claimedAliases = LinkedHashSet<Method>()

    init {
        // 如果给定了source, 那么distance使用source+1, 没有给定的话, 那么distance=0
        this.distance = if (this.source == null) 0 else source.distance + 1

        // 如果给定了root的话, 那么使用给定的root, 不然的话, 使用this作为root
        this.root = source?.root ?: this

        // 当前注解当中的属性方法
        this.attributes = AttributeMethods.forAnnotationType(annotationType)

        // 初始化MirrorSets
        this.mirrorSets = MirrorSets()

        // 初始化aliasMappings
        this.aliasMappings = IntArray(attributes.size) { -1 }
        // 初始化conventionMappings
        this.conventionMappings = IntArray(attributes.size) { -1 }
        // 初始化annotationValueMappings
        this.annotationValueMappings = IntArray(attributes.size) { -1 }
        // 初始化AnnotationValueSource
        this.annotationValueSource = Array(attributes.size) { null }

        // 解析aliasedBy
        this.aliasedBy = resolveAliasedForTargets()

        // 处理Alias关系
        processAliases()
    }

    /**
     * 处理Aliases
     */
    private fun processAliases() {
        val aliases = ArrayList<Method>()

        // 遍历当前注解当中的所有的属性方法, 去进行处理
        for (i in 0 until attributes.size) {
            aliases.clear()
            aliases += attributes[i]

            // 收集所有通过@AliasFor去指向当前正在进行处理的属性方法的那些属性方法
            collectAliases(aliases)

            // 如果size>1, 代表存在有指向attributes[i]的属性方法, 需要处理alias关系
            // 这种情况下, 我们需要找到root当中指向了当前属性方法的root属性方法的index
            if (aliases.size > 1) {
                processAliases(i, aliases)
            }
        }
    }

    /**
     * 找到当前Meta注解, 以及它的所有source当中, 所有的指向了当前的注解的所有的属性方法
     * 比如A标注了B作为Meta, B标注了C作为Meta, 当前是C的话, 它会遍历C当中的所有的属性方法, 去A/B当中去找到指向C的那些属性方法
     *
     * @param aliases aliases队列, 最终所有的指向这些属性方法的属性方法都将会收集到这里
     */
    private fun collectAliases(aliases: MutableList<Method>) {
        var mapping: AnnotationTypeMapping? = this
        // 从this开始, 遍历它的所有的source, 找到通过@AliasFor指向了当前注解当中的属性的那些方法
        while (mapping != null) {

            // 遍历队列当中的所有的属性方法, 去找到指向该属性方法的所有的@AliasFor方法
            for (j in 0 until aliases.size) {
                val additional = mapping.aliasedBy[aliases[j]]
                if (additional != null) {
                    aliases += additional
                }
            }
            mapping = mapping.source  // collect source
        }
    }

    /**
     * 处理当前注解属性对应的Aliases关系
     *
     * @param attributeIndex 当前正在去处理的属性方法index
     * @return 通过@AliasFor指向该属性方法的所有的属性方法(包含当前注解以及所有的source注解当中的属性方法)
     */
    private fun processAliases(attributeIndex: Int, aliases: MutableList<Method>) {
        // 从root注解当中, 去找到第一个有指向当前正在处理的属性方法的属性方法的index
        val rootAttributeIndex = getFirstRootAttributeIndex(aliases)
        var mapping: AnnotationTypeMapping? = this

        while (mapping != null) {
            // 如果root注解当中有指向当前属性方法的属性方法, 那么将aliasMappings对应的index的元素去指向rootAttributeIndex
            if (rootAttributeIndex != -1 && mapping !== this.root) {
                for (i in 0 until mapping.attributes.size) {
                    if (aliases.contains(mapping.attributes[i])) {
                        // i是mapping的有@AliasFor的属性方法的index, rootAttributeIndex是该属性对应的root注解的attributeIndex
                        mapping.aliasMappings[i] = rootAttributeIndex
                    }
                }
            }

            // 更新mirrorSets
            mapping.mirrorSets.updateFrom(aliases)
            mapping.claimedAliases += aliases

            // 如果mapping.annotation不为空, 说明当前不是root注解
            if (mapping.annotation != null) {
                val resolvedMirrors =
                    mapping.mirrorSets.resolve(null, mapping.annotation, ReflectionUtils::invokeMethod)

                for (i in 0 until mapping.attributes.size) {
                    if (aliases.contains(mapping.attributes[i])) {
                        this.annotationValueMappings[attributeIndex] = resolvedMirrors[i]
                        this.annotationValueSource[attributeIndex] = mapping
                    }
                }

            }

            mapping = mapping.source  // process source
        }
    }

    /**
     * 从rootAttribute当中找到第一个存在于aliases列表当中的属性方法
     *
     * @param aliases aliases列表
     * @return rootAttribute当中第一个有@AliasFor指向当前注解当中的属性的方法位置index
     */
    private fun getFirstRootAttributeIndex(aliases: List<Method>): Int {
        val rootAttributes = this.root.attributes
        for (i in 0 until rootAttributes.size) {
            if (aliases.contains(rootAttributes[i])) {
                return i
            }
        }
        return -1
    }

    /**
     * 解析AliasedFor指向的目标注解属性方法
     *
     * @return AliasedBy, Key是@AliasFor注解指向的目标注解属性方法, Value是标注了当前注解当中的@AliasFor注解的属性方法
     */
    private fun resolveAliasedForTargets(): Map<Method, List<Method>> {
        val aliasedBy = HashMap<Method, MutableList<Method>>()
        for (i in 0 until attributes.size) {
            val attribute = attributes[i]
            val aliasFor = AnnotationsScanner.getDeclaredAnnotation(attribute, AliasFor::class.java)
            if (aliasFor != null) {
                // 计算@AliasFor注解指向的目标属性
                val target = resolveAliasTarget(attribute, aliasFor)

                // Key-target, Value-attribute
                aliasedBy.computeIfAbsent(target) { ArrayList() }.add(attribute)
            }
        }
        return aliasedBy
    }

    /**
     * 计算一个标注了@AliasFor的属性方法指向的目标注解属性方法
     *
     * @param attribute 标注了@AliasFor注解的注解属性方法
     * @param aliasFor 该属性方法上的@AliasFor注解
     * @return 找到的@AliasFor指向的目标方法
     */
    private fun resolveAliasTarget(attribute: Method, aliasFor: AliasFor): Method =
        resolveAliasTarget(attribute, aliasFor, true)

    /**
     * 计算一个标注了@AliasFor的属性方法指向的目标注解属性方法
     *
     * @param attribute 标注了@AliasFor注解的注解属性方法
     * @param aliasFor 该属性方法上的@AliasFor注解
     * @param checkAliasPair 是否要检查@AliasFor镜像的情况
     * @return 找到的@AliasFor要去进行指向的目标方法
     */
    private fun resolveAliasTarget(attribute: Method, aliasFor: AliasFor, checkAliasPair: Boolean): Method {
        // 如果@AliasFor注解的attribute和value属性都被配置了的话, 那么丢出异常
        if (StringUtils.hasText(aliasFor.attribute) && StringUtils.hasText(aliasFor.value)) {
            throw AnnotationConfigurationException(
                String.format(
                    "In @AliasFor on '%s' attribute 'attribute' and its alias 'value' are present with values of '%s' and '%s', but only one is permitted.",
                    AttributeMethods.describe(attribute),
                    aliasFor.attribute,
                    aliasFor.value
                )
            )
        }

        // 解析得到要去指向的目标注解(默认为当前注解)
        var targetAnnotation = aliasFor.annotation.java
        if (targetAnnotation == Annotation::class.java) {
            targetAnnotation = this.annotationType
        }

        // 解析要去指向的目标注解的属性名(aliasFor.attribute->aliasFor.value->attribute.name)
        var targetAttributeName = aliasFor.attribute
        if (!StringUtils.hasText(targetAttributeName)) {
            targetAttributeName = aliasFor.value
        }
        if (!StringUtils.hasText(targetAttributeName)) {
            targetAttributeName = attribute.name
        }

        // 根据AnnotationType和AttributeName, 去找到目标注解上的目标属性
        val target = AttributeMethods.forAnnotationType(targetAnnotation).get(targetAttributeName)

        // 如果target==null, 代表没有找到合适的目标属性...需要报告异常情况
        if (target == null) {
            if (targetAnnotation == this.annotationType) {
                throw AnnotationConfigurationException(
                    String.format(
                        "@AliasFor declaration on %s declares an alias for '%s' which is not present.",
                        AttributeMethods.describe(attribute),
                        targetAttributeName
                    )
                )
            }
            throw AnnotationConfigurationException(
                String.format(
                    "%s is declared as an @AliasFor nonexistent %s.",
                    AttributeMethods.describe(attribute),
                    AttributeMethods.describe(targetAnnotation, targetAttributeName)
                )
            )
        }

        // 如果指向的目标属性是自己的话, 需要报告异常
        if (attribute == target) {
            throw AnnotationConfigurationException(
                String.format(
                    "@AliasFor declaration on %s points to itself. Specify 'annotation' to point to a same-named attribute on a meta-annotation.",
                    AttributeMethods.describe(attribute)
                )
            )
        }

        // 检查两者的返回值类型是否匹配? 如果返回值类型不兼容的话, 需要报告异常
        if (!isCompatibleReturnType(attribute.returnType, target.returnType)) {
            throw AnnotationConfigurationException(
                String.format(
                    "Misconfigured aliases: %s and %s must declare the same return type.",
                    AttributeMethods.describe(attribute),
                    AttributeMethods.describe(target)
                )
            )
        }

        // 如果@AliasFor指向的目标属性方法也是当前注解的话, 有可能是镜像的AliasPair
        if (checkAliasPair && isAliasPair(target)) {
            val targetAliasFor = target.getAnnotation(AliasFor::class.java)
            if (targetAliasFor != null) {
                val mirror = resolveAliasTarget(target, targetAliasFor, false)
                // 如果A->B, 但是B->C的话, 这种情况是不应该出现的; A->B的话, 那么只能B->A
                if (mirror != attribute) {
                    throw AnnotationConfigurationException(
                        String.format(
                            "%s must be declared as an @AliasFor %s, not %s.",
                            AttributeMethods.describe(target),
                            AttributeMethods.describe(attribute),
                            AttributeMethods.describe(mirror)
                        )
                    )
                }
            }
        }

        // 返回@AliasFor指向的目标属性方法
        return target
    }

    /**
     * 检查是否是一个Alias的Pair?
     *
     * @param target @AliasFor指向的目标属性
     * @return 如果该属性所在的类, 就是当前的注解
     */
    private fun isAliasPair(target: Method): Boolean = this.annotationType == target.declaringClass

    /**
     * 是否是兼容的返回值类型
     *
     * @param attributeType attributeType
     * @param targetType targetType
     * @return 如果兼容的话, return true; 否则return false
     */
    private fun isCompatibleReturnType(attributeType: Class<*>, targetType: Class<*>): Boolean {
        return attributeType == targetType || attributeType == targetType.componentType
    }

    /**
     * 根据AttributeIndex, 去找到root注解当中通过@AliasFor去指向了index对应的属性的root属性的index
     *
     * @param attributeIndex 当前注解当中的属性方法的index
     * @return root注解对应的属性方法的index
     */
    fun getAliasMapping(attributeIndex: Int): Int = aliasMappings[attributeIndex]

    fun getConventionMapping(attributeIndex: Int): Int = conventionMappings[attributeIndex]

    fun getMappedAnnotationValue(attributeIndex: Int, metaAnnotationOnly: Boolean): Any? {
        return null
    }

    /**
     * 每个AnnotationTypeMapping维护了一个MirrorSets
     */
    inner class MirrorSets {
        /**
         * MirrorSets
         */
        private var mirrorSets: Array<MirrorSet> = emptyArray()

        /**
         * assigned
         */
        private val assigned: Array<MirrorSet?> = arrayOfNulls(attributes.size)

        /**
         * MirrorSet的size
         */
        val size: Int
            get() = mirrorSets.size

        /**
         * 根据index去获取对应位置的MirrorSet
         *
         * @param index index
         * @return MirrorSet
         */
        operator fun get(index: Int): MirrorSet = mirrorSets[index]

        /**
         * 根据index去获取对应位置的Assigned MirrorSet
         *
         * @param index index
         * @return MirrorSet
         */
        @Nullable
        fun getAssigned(index: Int): MirrorSet? = this.assigned[index]

        @Suppress("UNCHECKED_CAST")
        fun updateFrom(aliases: List<Method>) {
            var mirrorSet: MirrorSet? = null
            var size = 0
            var last = -1

            // 遍历当前注解当中的所有属性方法
            for (i in 0 until attributes.size) {
                val attribute = attributes[i]

                // 如果这个属性方法, 包含在@AliasFor方法指向的别名当中的话, 那么需要为该属性方法去构建MirrorSet
                if (aliases.contains(attribute)) {
                    size++

                    // size>1, 说明当前注解当中存在有多个属性是互相为Mirror的
                    // 也就是A->B, B->A, 存在有多个属性互相指向的属性
                    if (size > 1) {

                        // 如果之前还没初始化过的话, 那么先初始化该位置的MirrorSet
                        if (mirrorSet == null) {
                            mirrorSet = MirrorSet()
                            assigned[last] = mirrorSet
                        }

                        // 把Mirror位置的MirrorSet去初始化为同一个MirrorSet对象
                        this.assigned[i] = mirrorSet
                    }
                    last = i
                }
            }

            // 如果该注解当中存在有互相为Mirror的属性方法
            if (mirrorSet != null) {
                mirrorSet.update()
                val unique = LinkedHashSet(listOf(*assigned))
                unique.remove(null)

                // 更新MirrorSets
                this.mirrorSets = unique.toTypedArray() as Array<MirrorSet>
            }
        }

        fun resolve(
            @Nullable source: Any?, @Nullable annotation: Any?, valueExtractor: ValueExtractor
        ): IntArray {
            val result = IntArray(attributes.size) { it }

            for (i in 0 until size) {
                val mirrorSet = this[i]
                val resolved = mirrorSet.resolve(source, annotation, valueExtractor)

                for (j in 0 until mirrorSet.size) {
                    result[mirrorSet.indexes[j]] = resolved
                }
            }

            return result
        }

        inner class MirrorSet {

            /**
             * 镜像的属性方法的大小
             */
            var size = 0

            /**
             * indexes
             */
            val indexes = IntArray(attributes.size)

            /**
             * 根据MirrorSets的状态, 去更新当前MirrorSet最新状态
             */
            fun update() {
                this.size = 0
                // 将indexes全部初始化为-1
                Arrays.fill(indexes, -1)
                // 将互为镜像的那些属性方法的index, 保存到indexes当中去, i
                for (i in 0 until this@MirrorSets.assigned.size) {
                    if (this == this@MirrorSets.assigned[i]) {
                        this.indexes[this.size++] = i
                    }
                }
            }

            fun resolve(
                @Nullable source: Any?, @Nullable annotation: Any?, valueExtractor: ValueExtractor
            ): Int {
                var result = -1
                var lastValue: Any? = null
                for (i in 0 until size) {
                    val attribute = attributes[this.indexes[i]]
                    val value = valueExtractor.extract(attribute, annotation)

                    // TODO

                    result = this.indexes[i]
                    lastValue = value
                }

                return result
            }
        }
    }
}