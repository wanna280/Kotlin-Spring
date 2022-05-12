package com.wanna.framework.core.type

/**
 * 这是一个AnnotationMetadata，维护了注解的相关信息，是Spring当中对于一个类上标注的相关注解的描述
 */
interface AnnotationMetadata : AnnotatedTypeMetadata, ClassMetadata {

    /**
     * 获取标注的注解的类型集合(String)
     */
    fun getAnnotationTypes(): Set<String> {
        return getAnnotations().map { it.annotationClass::class.java.name }.toCollection(HashSet<String>())
    }

    /**
     * 是否直接标注了这个注解？
     */
    fun hasAnnotation(annotationName: String): Boolean {
        return getAnnotationTypes().contains(annotationName)
    }

    companion object {
        /**
         * 直接构建一个StandardAnnotationMetadata
         */
        fun introspect(clazz: Class<*>): AnnotationMetadata = StandardAnnotationMetadata.from(clazz)
    }
}