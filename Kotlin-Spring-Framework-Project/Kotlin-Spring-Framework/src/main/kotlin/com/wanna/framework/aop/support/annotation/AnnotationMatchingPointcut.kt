package com.wanna.framework.aop.support.annotation

import com.wanna.framework.aop.MethodMatcher
import com.wanna.framework.aop.Pointcut

/**
 * 基于注解的匹配的Pointcut，支持去匹配一个类上的注解信息
 *
 * @param annotationClass 要去进行匹配的注解类型
 */
open class AnnotationMatchingPointcut(val annotationClass: Class<out Annotation>) : Pointcut {

    // ClassFilter，用于提供注解的匹配工作
    private val classFilter = AnnotationClassFilter(annotationClass)

    // MethodMatcher
    private val methodMatcher = MethodMatcher.TRUE

    override fun getClassFilter() = this.classFilter

    override fun getMethodMatcher() = this.methodMatcher
}