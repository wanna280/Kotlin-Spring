package com.wanna.framework.core.type.filter

/**
 * 类型的过滤器，支持去对一个类的各个方面去进行匹配，比如匹配注解、匹配父类等方式
 *
 * @see AssignableTypeFilter
 * @see AnnotationTypeFilter
 */
interface TypeFilter {
    fun matches(clazz: Class<*>?) : Boolean
}