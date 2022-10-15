package com.wanna.framework.core.type.filter

import com.wanna.framework.util.ClassUtils

/**
 * 匹配类型，判断给定的类是否是parentClass的子类？
 */
open class AssignableTypeFilter(private val parentClass: Class<*>) : TypeFilter {
    override fun matches(clazz: Class<*>?): Boolean {
        return ClassUtils.isAssignFrom(parentClass, clazz)
    }
}