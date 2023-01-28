package com.wanna.framework.core.type

import com.wanna.framework.core.annotation.MergedAnnotations
import com.wanna.framework.util.ReflectionUtils

/**
 * 这是一个标准的的AnnotationMetadata的实现
 *
 * @param introspectedClass 要去描述的目标类
 */
open class StandardAnnotationMetadata(introspectedClass: Class<*>) : StandardClassMetadata(introspectedClass),
    AnnotationMetadata {

    /**
     * MergedAnnotations
     */
    private val annotations = MergedAnnotations.from(introspectedClass)

    /**
     * 获取到一个类上的注解的merge结果[MergedAnnotations]
     *
     * @return MergedAnnotations
     */
    override fun getAnnotations(): MergedAnnotations = this.annotations

    override fun getAnnotatedMethods(annotationName: String): Set<MethodMetadata> {
        val methods = LinkedHashSet<MethodMetadata>()
        ReflectionUtils.doWithLocalMethods(introspectedClass) {
            it.annotations.forEach { ann ->
                if (ann.annotationClass.java.name == annotationName) {
                    methods += StandardMethodMetadata(it)
                }
            }
        }
        return methods
    }

    /**
     * toString, 采用[introspectedClass]的类名去进行生成
     *
     * @return toString
     */
    override fun toString(): String = introspectedClass.name


    companion object {
        /**
         * 为一个类去进行构建[StandardAnnotationMetadata]的工厂方法
         *
         * @param introspectedClass 要去进行描述的类
         * @return StandardAnnotationMetadata
         */
        @JvmStatic
        fun from(introspectedClass: Class<*>): StandardAnnotationMetadata {
            return StandardAnnotationMetadata(introspectedClass)
        }
    }
}