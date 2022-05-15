package com.wanna.framework.core.type.filter

/**
 * 类型的过滤器
 */
interface TypeFilter {
    fun matches(clazz: Class<*>?) : Boolean
}