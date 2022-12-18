package com.wanna.framework.core.type

/**
 * 这是对一个方法的元信息进行维护的组件
 */
interface MethodMetadata : AnnotatedTypeMetadata {
    /**
     * 获取方法名
     *
     * @return methodName
     */
    fun getMethodName(): String

    /**
     * 获取方法所被定义的类
     *
     * @return declaringClassName
     */
    fun getDeclaringClassName(): String

    /**
     * 获取方法的返回值类型
     *
     * @return 返回值类型的全类名
     */
    fun getReturnTypeName(): String

    /**
     * 方法是否是抽象的
     *
     * @return 如果该方法是抽象的，return true；否则return false
     */
    fun isAbstract(): Boolean

    /**
     * 方法是否是static的？
     *
     * @return 如果该方法是static的，return true；否则return false
     */
    fun isStatic(): Boolean

    /**
     * 方法是否可以被重写？
     *
     * @return 该方法如果可以重写，那么return true；否则return false
     */
    fun isOverridable(): Boolean = !isFinal() && !isPrivate() && !isStatic()

    /**
     * 方法是否是private的？
     *
     * @return 如果该方法是private的，return true；否则return false
     */
    fun isPrivate() : Boolean

    /**
     * 方法是否是Final的？
     *
     * @return 如果该方法是final的，return true；否则return false
     */
    fun isFinal(): Boolean

    /**
     * 获取该方法上标注的注解的类型集合(String)
     *
     * @return 该方法的注解类型的全类名列表
     */
    fun getAnnotationTypes(): Set<String> {
        return getAnnotations().map { it.annotationClass.java.name }.toCollection(HashSet<String>())
    }

    /**
     * 是否直接标注了这个注解？
     *
     * @param annotationName 注解全类名
     * @return 如果标注了该注解，return true；否则return false
     */
    fun hasAnnotation(annotationName: String): Boolean {
        return getAnnotationTypes().contains(annotationName)
    }
}