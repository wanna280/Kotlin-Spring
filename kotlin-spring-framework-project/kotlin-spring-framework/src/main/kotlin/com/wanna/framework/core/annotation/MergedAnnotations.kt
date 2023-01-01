package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.lang.reflect.AnnotatedElement
import java.util.function.Predicate
import java.util.stream.Stream

/**
 * MergedAnnotations
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 */
interface MergedAnnotations : Iterable<MergedAnnotation<Annotation>> {

    /**
     * 检查MergedAnnotations当中是否存在有给定的注解?
     *
     * @param annotationType annotationType
     * @return 如果存在有给定的注解的话, return true; 否则return false
     */
    fun <A : Annotation> isPresent(annotationType: Class<A>): Boolean

    /**
     * 检查MergedAnnotations当中是否存在有给定的注解name的注解?
     *
     * @param annotationName annotationName
     * @return 如果存在有给定的注解的话, return true; 否则return false
     */
    fun isPresent(annotationName: String): Boolean

    /**
     * 检查MergedAnnotations当中是否直接标注有给定的注解?
     *
     * @param annotationType 要去进行检查的注解类型
     * @return 如果直接标注的话, 那么return true; 否则return false
     */
    fun <A : Annotation> isDirectPresent(annotationType: Class<A>): Boolean

    /**
     * 检查MergedAnnotations当中是否直接标注有给定的注解?
     *
     * @param annotationName AnnotationName
     * @return 如果直接标注的话, 那么return true; 否则return false
     */
    fun isDirectPresent(annotationName: String): Boolean

    /**
     * 从MergedAnnotations当中, 去根据AnnotationType去获取到该注解对应的合并之后的结果MergedAnnotation
     *
     * @param annotationType annotationType
     * @return MergedAnnotation(如果不存在的话，返回 [MergedAnnotation.missing])
     */
    fun <A : Annotation> get(annotationType: Class<A>): MergedAnnotation<A>

    /**
     * 从MergedAnnotations当中, 去根据AnnotationType去获取到该注解对应的合并之后的结果MergedAnnotation
     *
     * @param annotationType annotationType
     * @param predicate 支持去对要去进行返回的目标注解进行过滤, 只有在断言匹配的情况下才返回
     * @return MergedAnnotation(如果不存在的话，返回 [MergedAnnotation.missing])
     */
    fun <A : Annotation> get(
        annotationType: Class<A>, @Nullable predicate: Predicate<in MergedAnnotation<A>>?
    ): MergedAnnotation<A>

    /**
     * 从MergedAnnotations当中, 去根据AnnotationType去获取到该注解对应的合并之后的结果MergedAnnotation
     *
     * @param annotationType annotationType
     * @param predicate 支持去对要去进行返回的目标注解进行过滤, 只有在断言匹配的情况下才返回
     * @param selector 当遇到了多个匹配的注解的情况下, 应该要选取哪个去作为最终的注解?(默认是采用距离最近的一个)
     * @return MergedAnnotation(如果不存在的话，返回 [MergedAnnotation.missing])
     */
    fun <A : Annotation> get(
        annotationType: Class<A>,
        @Nullable predicate: Predicate<in MergedAnnotation<A>>?,
        @Nullable selector: MergedAnnotationSelector<A>?
    ): MergedAnnotation<A>

    /**
     * 从MergedAnnotations当中, 根据AnnotationName去获取到该注解对应的合并之后的结果MergedAnnotation
     *
     * @param annotationName AnnotationName
     * @return MergedAnnotation(如果不存在的话，返回 [MergedAnnotation.missing])
     */
    fun <A : Annotation> get(annotationName: String): MergedAnnotation<A>

    /**
     * 从MergedAnnotations当中, 根据AnnotationName去获取到该注解对应的合并之后的结果MergedAnnotation
     *
     * @param annotationName AnnotationName
     * @param predicate 支持去对要去进行返回的目标注解进行过滤, 只有在断言匹配的情况下才返回
     * @return MergedAnnotation(如果不存在的话，返回 [MergedAnnotation.missing])
     */
    fun <A : Annotation> get(
        annotationName: String, @Nullable predicate: Predicate<in MergedAnnotation<A>>?
    ): MergedAnnotation<A>

    /**
     * 从MergedAnnotations当中, 根据AnnotationName去获取到该注解对应的合并之后的结果MergedAnnotation
     *
     * @param annotationName AnnotationName
     * @param predicate 支持去对要去进行返回的目标注解进行过滤, 只有在断言匹配的情况下才返回
     * @param selector 当遇到了多个匹配的注解的情况下, 应该要选取哪个去作为最终的注解?(默认是采用距离最近的一个)
     * @return MergedAnnotation(如果不存在的话，返回 [MergedAnnotation.missing])
     */
    fun <A : Annotation> get(
        annotationName: String,
        @Nullable predicate: Predicate<in MergedAnnotation<A>>?,
        @Nullable selector: MergedAnnotationSelector<A>?
    ): MergedAnnotation<A>

    /**
     * 获取到迭代所有的注解的Stream
     *
     * @return Stream of MergedAnnotation
     */
    fun stream(): Stream<MergedAnnotation<Annotation>>

    /**
     * 迭代MergedAnnotation当中的所有的给定的注解类型的MergedAnnotation的Stream
     *
     * @param annotationType 要去进行迭代注解类型AnnotationType
     * @return Stream of MergedAnnotation for given AnnotationType
     */
    fun <A : Annotation> stream(annotationType: Class<A>): Stream<MergedAnnotation<A>>

    /**
     * 迭代MergedAnnotation当中的所有的给定的注解类型的MergedAnnotation的Stream
     *
     * @param annotationName 要去进行迭代注解的类名
     * @return Stream of MergedAnnotation for given AnnotationType
     */
    fun <A : Annotation> stream(annotationName: String): Stream<MergedAnnotation<A>>

    /**
     * 注解的搜索策略枚举
     */
    enum class SearchStrategy {
        /**
         * 只去处理直接注解
         */
        DIRECT,

        /**
         * 不仅去处理直接注解, 还得去处理继承的注解
         */
        INHERITED_ANNOTATIONS,

        /**
         * 不仅去处理直接注解, 还得处理它的父类
         */
        SUPERCLASS,

        /**
         * 不仅去处理直接注解, 还得处理父类/接口
         */
        TYPE_HIERARCHY,

        /**
         * 不仅处理直接注解, 还去处理父类/接口/外部类
         */
        TYPE_HIERARCHY_AND_ENCLOSING_CLASSES
    }

    companion object {

        /**
         * 针对给定的[AnnotatedElement], 去构建出来去描述该元素的[MergedAnnotations]
         *
         * @param element 要去进行描述的目标方法/字段/类/构造器
         * @return 描述该元素的MergedAnnotations
         */
        @JvmStatic
        fun from(element: AnnotatedElement): MergedAnnotations {
            return from(element, SearchStrategy.DIRECT)
        }

        @JvmStatic
        fun from(element: AnnotatedElement, searchStrategy: SearchStrategy): MergedAnnotations {
            return from(element, searchStrategy, null)
        }

        @JvmStatic
        fun from(
            element: AnnotatedElement, searchStrategy: SearchStrategy, repeatableContainers: RepeatableContainers?
        ): MergedAnnotations {
            return from(element, searchStrategy, repeatableContainers, AnnotationFilter.PLAIN)
        }

        @JvmStatic
        fun from(
            element: AnnotatedElement,
            searchStrategy: SearchStrategy,
            repeatableContainers: RepeatableContainers?,
            annotationFilter: AnnotationFilter
        ): MergedAnnotations {
            return TypeMappedAnnotations.from(element, searchStrategy, repeatableContainers, annotationFilter)
        }

        @JvmStatic
        fun from(
            source: Any,
            annotations: Array<Annotation>,
            repeatableContainers: RepeatableContainers?,
            annotationFilter: AnnotationFilter
        ): MergedAnnotations {
            return TypeMappedAnnotations.from(source, annotations, repeatableContainers, annotationFilter)
        }

        /**
         * MergedAnnotations的构建的工厂方法
         *
         * @param mergedAnnotations MergedAnnotation列表
         * @return MergedAnnotations
         */
        @JvmStatic
        fun of(mergedAnnotations: Array<MergedAnnotation<*>>): MergedAnnotations {
            return MergedAnnotationsCollection.of(mergedAnnotations)
        }
    }
}