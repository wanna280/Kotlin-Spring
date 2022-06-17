package com.wanna.framework.core.type

/**
 * 这是一个AnnotationMetadata，维护了一个类上的注解的相关信息，是Spring当中对于一个类上标注的相关注解的描述
 *
 * @see AnnotatedTypeMetadata
 * @see StandardAnnotationMetadata
 */
interface AnnotationMetadata : AnnotatedTypeMetadata, ClassMetadata {

    /**
     * 获取标注的注解的类型集合(String)
     */
    fun getAnnotationTypes(): Set<String> {
        return getAnnotations().map { it.annotationClass::class.java.name }.toSet()
    }

    /**
     * 该类上是否直接标注了这个注解？
     *
     * @param annotationName 注解的全类名
     */
    fun hasAnnotation(annotationName: String): Boolean {
        return getAnnotationTypes().contains(annotationName)
    }

    /**
     * 该类当中是否有直接标注了某个注解的方法
     *
     * @param annotationName 注解全类名
     * @return 如果找到了标注给注解的方法，return true；否则，return false
     */
    fun hasAnnotatedMethods(annotationName: String): Boolean {
        return getAnnotatedMethods(annotationName).isNotEmpty()
    }

    /**
     * 获取该类当中标注了某个注解的方法的列表
     *
     * @param annotationName 要匹配的注解的全类名
     * @return 解析到的所有的该注解的方法列表
     */
    fun getAnnotatedMethods(annotationName: String): Set<MethodMetadata>

    companion object {
        /**
         * 直接构建一个StandardAnnotationMetadata
         *
         * @param clazz class
         * @return 构建好的AnnotationMetadata
         */
        @JvmStatic
        fun introspect(clazz: Class<*>): AnnotationMetadata = StandardAnnotationMetadata.from(clazz)
    }
}