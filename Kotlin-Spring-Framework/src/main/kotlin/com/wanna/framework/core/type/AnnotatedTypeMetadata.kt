package com.wanna.framework.core.type

/**
 * 这是一个被注解标注的的类型的Metadata信息
 */
interface AnnotatedTypeMetadata {
    /**
     * 获取类型上标注的注解信息
     */
    fun getAnnotations(): Array<Annotation>

    /**
     * 指定具体的注解name，去寻找到合适的注解的对应属性
     */
    fun getAnnotationAttributes(annotationName: String): Map<String, Any>

    /**
     * 指定具体的注解clazz，去寻找到合适的注解的对应属性
     */
    fun getAnnotationAttributes(annotationClass: Class<out Annotation>): Map<String, Any>
}