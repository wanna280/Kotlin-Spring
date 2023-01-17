package com.wanna.framework.core.type

import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.annotation.MergedAnnotations
import java.util.Collections
import java.util.stream.Collectors

/**
 * 这是一个AnnotationMetadata, 维护了一个类上的注解的相关信息, 是Spring当中对于一个类上标注的相关注解的描述
 *
 * @see AnnotatedTypeMetadata
 * @see StandardAnnotationMetadata
 */
interface AnnotationMetadata : AnnotatedTypeMetadata, ClassMetadata {

    /**
     * 获取直接标注的注解的类型集合(String)
     *
     * @return 直接标注的注解列表
     */
    fun getAnnotationTypes(): Set<String> {
        return getAnnotations().stream().filter { it.directPresent }.map { it.type.name }.collect(Collectors.toSet())
    }

    /**
     * 获取给定的注解的Meta注解列表, 比如A注解上标注了B注解, 那么A注解上去寻找Meta注解就会找到B...
     *
     * @param annotationName 待寻找的注解类名annotationName
     * @return 寻找到的所有的直接标注的Meta注解(找不到返回空的集合)
     */
    fun getMetaAnnotationTypes(annotationName: String): Set<String> {
        val mergedAnnotation = getAnnotations().get(annotationName, MergedAnnotation<Annotation>::directPresent)
        if (!mergedAnnotation.present) {
            return Collections.emptySet()
        }
        return MergedAnnotations.from(mergedAnnotation.type, MergedAnnotations.SearchStrategy.INHERITED_ANNOTATIONS)
            .map { it.type.name }.toSet()
    }

    /**
     * 该类上是否直接标注了这个注解? 
     *
     * @param annotationName 注解的全类名
     * @return 如果直接标注了该注解的话, 那么return true; 否则return false
     */
    fun hasAnnotation(annotationName: String): Boolean = getAnnotationTypes().contains(annotationName)

    /**
     * 该类当中是否有直接标注了某个注解的方法
     *
     * @param annotationName 注解全类名
     * @return 如果找到了标注给注解的方法, return true; 否则, return false
     */
    fun hasAnnotatedMethods(annotationName: String) = getAnnotatedMethods(annotationName).isNotEmpty()

    /**
     * 获取该类当中标注了某个注解的方法的列表
     *
     * @param annotationName 要匹配的注解的全类名
     * @return 解析到的所有的标注了该注解的方法列表
     */
    fun getAnnotatedMethods(annotationName: String): Set<MethodMetadata>

    companion object {
        /**
         * 直接根据给定的Class, 去为该类构建出来一个StandardAnnotationMetadata
         *
         * @param clazz 要去进行描述注解信息的Class
         * @return 为该类构建出来的AnnotationMetadata
         */
        @JvmStatic
        fun introspect(clazz: Class<*>): AnnotationMetadata = StandardAnnotationMetadata.from(clazz)
    }
}