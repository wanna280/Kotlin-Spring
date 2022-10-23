package com.wanna.framework.core.type.filter

import java.util.regex.Pattern

/**
 * 基于正则表达式的TypeFilter去对一个类去进行匹配
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/21
 */
open class RegexPatternTypeFilter(val pattern: Pattern) : TypeFilter {
    override fun matches(clazz: Class<*>?): Boolean {
        clazz ?: return false
        return pattern.matcher(clazz.name).matches()
    }
}