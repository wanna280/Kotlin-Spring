package com.wanna.framework.aop.support.annotation

import com.wanna.framework.aop.ClassFilter
import com.wanna.framework.core.annotation.AnnotatedElementUtils

/**
 * 支持去对注解去进行匹配的ClassFilter
 *
 * @param annotationClass 要去进行匹配的目标注解类型
 */
open class AnnotationClassFilter(val annotationClass: Class<out Annotation>) : ClassFilter {

    /**
     * 执行匹配逻辑，检查给定的类上是否有标志指定的注解(annotationClass)
     *
     * @param clazz 待去进行检查的目标类
     * @return 如果clazz上有目标注解，return true；否则return false
     */
    override fun matches(clazz: Class<*>): Boolean = AnnotatedElementUtils.isAnnotated(clazz, annotationClass)
}