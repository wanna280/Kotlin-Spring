package com.wanna.framework.core.type

import com.wanna.framework.core.annotation.MergedAnnotations

/**
 * 这是一个被注解标注的的类型的Metadata信息，支持去获取到注解的相关属性；
 * 主要有两类AnnotatedTypeMetadata的实现，一类是方法的Metadata(MethodMetadata)，另外一类是类的Metadata(AnnotationMetadata)；
 *
 * @see MethodMetadata
 * @see AnnotationMetadata
 */
interface AnnotatedTypeMetadata {

    /**
     * 获取MergedAnnotations
     *
     * @return MergedAnnotations
     */
    fun getAnnotations(): MergedAnnotations


    /**
     * 指定具体的注解name，去寻找到合适的注解的对应属性
     *
     * @param annotationName 注解的全类名
     * @return 解析到的注解属性，如果该注解没有属性，那么return empty
     */
    fun getAnnotationAttributes(annotationName: String): Map<String, Any>

    /**
     * 指定具体的注解clazz，去寻找到合适的注解的对应属性
     *
     * @param annotationClass 注解的类型
     * @return 解析到的注解属性，如果该注解没有属性，那么return empty
     */
    fun getAnnotationAttributes(annotationClass: Class<out Annotation>): Map<String, Any> =
        getAnnotationAttributes(annotationClass.name)

    /**
     * 判断该类型上是否标注了某个注解？
     *
     * @param annotationName 注解的全类名
     * @return 如果标注了，那么return true；否则return false
     */
    fun isAnnotated(annotationName: String): Boolean =
        getAnnotations().map { it::class.java.name }.contains(annotationName)
}