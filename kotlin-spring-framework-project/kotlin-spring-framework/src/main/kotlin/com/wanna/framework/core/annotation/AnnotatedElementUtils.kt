package com.wanna.framework.core.annotation

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.lang.Nullable
import java.lang.reflect.AnnotatedElement

/**
 * AnnotatedElement的工具类，负责桥接SpringCore包当中的AnnotatedElementUtils
 */
object AnnotatedElementUtils {

    /**
     * 检查给定的目标AnnotatedElement上是否直接/间接标注了annotationType对应的注解
     *
     * @param element 要去进行寻找注解的目标AnnotatedElement
     * @param annotationType 要去寻找的目标注解类型
     * @return 如果该元素上包含了目标注解return true; 否则return false
     */

    @JvmStatic
    fun isAnnotated(element: AnnotatedElement, annotationType: Class<out Annotation>): Boolean {
        return getAnnotations(element)
            .isPresent(annotationType)
    }

    /**
     * 检查给定的目标AnnotatedElement上是否直接/间接标注了annotationName对应的注解
     *
     * @param element 要去进行寻找注解的目标AnnotatedElement
     * @param annotationClassName 注解的类名
     * @return 如果该元素上包含了目标注解return true; 否则return false
     */
    @JvmStatic
    fun isAnnotated(element: AnnotatedElement, annotationClassName: String): Boolean {
        return getAnnotations(element)
            .isPresent(annotationClassName)
    }

    /**
     * 从给定的目标AnnotatedElement上去寻找到指定类型(annotationType)的合并的注解
     *
     * @param element 要去进行寻找注解的目标AnnotatedElement
     * @param annotationType 要去寻找的目标注解类型
     * @return 获取到的目标注解; 如果注解不存在的话, return null
     */
    @Nullable
    @JvmStatic
    fun <A : Annotation> getMergedAnnotation(element: AnnotatedElement, annotationType: Class<A>): A? {
        return getAnnotations(element)
            .get(annotationType, null, MergedAnnotationSelectors.firstDirectlyDeclared())
            .synthesize(MergedAnnotation<A>::present)
            .orElse(null)
    }

    /**
     * 从目标元素上找到所有的类型匹配的注解的集合(搜索方式为INHERITED_ANNOTATIONS)
     *
     * @param element 要去进行寻找注解的目标AnnotatedElement
     * @param annotationType 要去寻找的目标注解类型
     * @return 获取到的目标注解的集合
     */
    @JvmStatic
    fun <A : Annotation> getAllMergedAnnotations(element: AnnotatedElement, annotationType: Class<A>): Set<A> {
        return getAnnotations(element)
            .stream(annotationType)
            .collect(MergedAnnotationCollectors.toAnnotationSet<A>())
    }

    /**
     * 从目标元素上找到所有的类型匹配的注解的集合(搜索方式为TYPE_HIERARCHY)
     *
     * @param element 要去进行寻找注解的目标AnnotatedElement
     * @param annotationType 要去寻找的目标注解类型
     * @return 获取到的目标注解的集合
     */
    @JvmStatic
    fun <A : Annotation> findAllMergedAnnotations(element: AnnotatedElement, annotationType: Class<A>): Set<A> {
        return findAnnotations(element)
            .stream(annotationType)
            .collect(MergedAnnotationCollectors.toAnnotationSet<A>())
    }

    /**
     * 判断目标元素上是否有给定的注解(支持使用继承的方式去进行检查父类)
     *
     * @param element 目标元素(方法/字段等)
     * @param annotationType 要去进行匹配的注解
     * @return 如果存在有目标注解的户，return true；否则return false
     */
    @JvmStatic
    fun hasAnnotation(element: AnnotatedElement, annotationType: Class<out Annotation>): Boolean {
        return findAnnotations(element)
            .isPresent(annotationType)
    }

    /**
     * 从给定的目标AnnotatedElement上去寻找到指定类型(annotationType)的合并的注解属性信息
     *
     * @param element 要去进行寻找注解的目标AnnotatedElement
     * @param annotationType 要去寻找的目标注解类型
     * @return 获取到的目标注解的属性信息; 如果注解不存在的话, return null
     */
    @Nullable
    @JvmStatic
    fun getMergedAnnotationAttributes(
        element: AnnotatedElement, annotationType: Class<out Annotation>
    ): AnnotationAttributes? {
        val mergedAnnotation = getAnnotations(annotationType).get(
            annotationType, null, MergedAnnotationSelectors.firstDirectlyDeclared()
        )
        return getAnnotationAttributes(mergedAnnotation, false, true)
    }

    @JvmStatic
    private fun getAnnotations(annotatedElement: AnnotatedElement): MergedAnnotations {
        return MergedAnnotations.from(
            annotatedElement, MergedAnnotations.SearchStrategy.INHERITED_ANNOTATIONS, RepeatableContainers.none()
        )
    }

    private fun findAnnotations(annotatedElement: AnnotatedElement): MergedAnnotations {
        return MergedAnnotations.from(
            annotatedElement, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none()
        )
    }

    /**
     * 为给定的MergedAnnotation去获取到AnnotationAttributes
     *
     * @param mergedAnnotation MergedAnnotation
     * @param classValueAsString 是否需要将Class转为String
     * @param nestedAnnotationsAsMap 是否需要将注解转换为Map
     * @return 如果该注解不存在return null; 存在的话, return该注解的属性值Map
     */
    @Nullable
    @JvmStatic
    private fun <A : Annotation> getAnnotationAttributes(
        mergedAnnotation: MergedAnnotation<A>, classValueAsString: Boolean, nestedAnnotationsAsMap: Boolean
    ): AnnotationAttributes? {
        if (!mergedAnnotation.present) {
            return null
        }
        val adapts = MergedAnnotation.Adapt.values(classValueAsString, nestedAnnotationsAsMap)
        return mergedAnnotation.asAnnotationAttributes(*adapts)
    }
}