package com.wanna.framework.aop

/**
 * 支持对一个类去进行匹配的ClassFIlter
 */
fun interface ClassFilter {
    companion object {
        @JvmField
        val TRUE = TrueClassFilter.INSTANCE
    }

    fun matches(clazz: Class<*>): Boolean
}