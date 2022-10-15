package com.wanna.framework.aop

/**
 * 不对类进行匹配，直接return true
 */
class TrueClassFilter : ClassFilter {
    companion object {
        @JvmField
        val INSTANCE = TrueClassFilter()
    }

    override fun matches(clazz: Class<*>): Boolean {
        return true
    }
}