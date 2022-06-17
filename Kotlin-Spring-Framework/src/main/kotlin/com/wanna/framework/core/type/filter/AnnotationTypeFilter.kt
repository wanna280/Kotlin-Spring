package com.wanna.framework.core.type.filter

import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 匹配注解，判断一个类上是否标注了某个注解？
 *
 * @param annotationType 要去匹配的注解类型
 */
open class AnnotationTypeFilter(private val annotationType: Class<out Annotation>) : TypeFilter {
    override fun matches(clazz: Class<*>?): Boolean {
        return clazz != null && AnnotatedElementUtils.getMergedAnnotation(clazz, annotationType) != null
    }
}