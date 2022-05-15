package com.wanna.framework.core.type

/**
 * 这是对一个方法的元信息进行维护的组件
 */
interface MethodMetadata : AnnotatedTypeMetadata {
    /**
     * 获取方法名
     */
    fun getMethodName(): String

    /**
     * 获取方法所被定义的类
     */
    fun getDeclaringClassName(): String

    /**
     * 获取方法的返回值类型
     */
    fun getReturnTypeName(): String

    /**
     * 方法是否是抽象的
     */
    fun isAbstract(): Boolean

    /**
     * 方法是否是static的？
     */
    fun isStatic(): Boolean

    /**
     * 方法是否可以被重写？
     */
    fun isOverridable(): Boolean

    /**
     * 方法是否是private的？
     */
    fun isPrivate() : Boolean

    /**
     * 方法是否是Final的？
     */
    fun isFinal(): Boolean

    /**
     * 获取标注的注解的类型集合(String)
     */
    fun getAnnotationTypes(): Set<String> {
        return getAnnotations().map { it.annotationClass.java.name }.toCollection(HashSet<String>())
    }

    /**
     * 是否直接标注了这个注解？
     */
    fun hasAnnotation(annotationName: String): Boolean {
        return getAnnotationTypes().contains(annotationName)
    }
}