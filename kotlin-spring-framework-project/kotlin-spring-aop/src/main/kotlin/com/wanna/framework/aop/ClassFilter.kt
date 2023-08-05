package com.wanna.framework.aop

/**
 * 支持对一个类去进行匹配的ClassFilter
 *
 * @see TrueClassFilter
 */
fun interface ClassFilter {
    companion object {

        /**
         * 暴露永远匹配成功的[ClassFilter]单例对象
         */
        @JvmField
        val TRUE = TrueClassFilter
    }

    fun matches(clazz: Class<*>): Boolean
}