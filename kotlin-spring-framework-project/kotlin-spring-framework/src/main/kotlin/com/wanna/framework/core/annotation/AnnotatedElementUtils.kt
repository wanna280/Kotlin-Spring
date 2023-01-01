package com.wanna.framework.core.annotation

import com.wanna.framework.core.type.GlobalTypeSwitch
import com.wanna.framework.lang.Nullable
import org.springframework.core.annotation.AnnotatedElementUtils
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
        if (GlobalTypeSwitch.isAnnotatedElementUtilsOpen()) {
            return getAnnotations(element).isPresent(annotationType)
        }
        return AnnotatedElementUtils.isAnnotated(element, annotationType)
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
        if (GlobalTypeSwitch.isAnnotatedElementUtilsOpen()) {
            return getAnnotations(element).isPresent(annotationClassName)
        }
        return AnnotatedElementUtils.isAnnotated(element, annotationClassName)
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
        if (GlobalTypeSwitch.isAnnotatedElementUtilsOpen()) {
            return getAnnotations(element).get(annotationType, null, MergedAnnotationSelectors.firstDirectlyDeclared())
                .synthesize(MergedAnnotation<A>::present).orElse(null)
        }
        return AnnotatedElementUtils.getMergedAnnotation(element, annotationType)
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
        if (GlobalTypeSwitch.isAnnotatedElementUtilsOpen()) {
            return getAnnotations(element)
                .stream(annotationType)
                .collect(MergedAnnotationCollectors.toAnnotationSet<A>())
        }
        return AnnotatedElementUtils.getAllMergedAnnotations(element, annotationType)
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
        if (GlobalTypeSwitch.isAnnotatedElementUtilsOpen()) {
            return findAnnotations(element)
                .stream(annotationType)
                .collect(MergedAnnotationCollectors.toAnnotationSet<A>())
        }
        return AnnotatedElementUtils.findAllMergedAnnotations(element, annotationType)
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
        if (GlobalTypeSwitch.isAnnotatedElementUtilsOpen()) {
            return findAnnotations(element).isPresent(annotationType)
        }
        return AnnotatedElementUtils.hasAnnotation(element, annotationType)
    }

    @JvmStatic
    private fun getAnnotations(annotatedElement: AnnotatedElement): MergedAnnotations {
        return MergedAnnotations.from(
            annotatedElement,
            MergedAnnotations.SearchStrategy.INHERITED_ANNOTATIONS,
            RepeatableContainers.none()
        )
    }

    private fun findAnnotations(annotatedElement: AnnotatedElement): MergedAnnotations {
        return MergedAnnotations.from(
            annotatedElement,
            MergedAnnotations.SearchStrategy.TYPE_HIERARCHY,
            RepeatableContainers.none()
        )
    }
}