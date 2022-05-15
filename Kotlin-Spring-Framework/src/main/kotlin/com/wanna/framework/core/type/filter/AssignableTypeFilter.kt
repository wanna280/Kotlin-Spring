package com.wanna.framework.core.type.filter

import com.wanna.framework.core.util.ClassUtils

/**
 * 匹配类型
 */
open class AssignableTypeFilter(private val parentClass: Class<*>) : TypeFilter {
    override fun matches(clazz: Class<*>?): Boolean {
        return ClassUtils.isAssignFrom(parentClass, clazz)
    }
}