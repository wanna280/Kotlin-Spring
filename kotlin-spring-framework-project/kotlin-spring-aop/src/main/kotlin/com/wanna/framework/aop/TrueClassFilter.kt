package com.wanna.framework.aop

/**
 * 不对类进行匹配, 直接return true
 */
object TrueClassFilter : ClassFilter {
    override fun matches(clazz: Class<*>): Boolean {
        return true
    }
}