package com.wanna.framework.util

class ClassUtils {
    companion object {
        /**
         * 判断childClass是否是parentClass的子类？
         */
        @JvmStatic
        fun isAssginFrom(parentClass: Class<*>, childClass: Class<*>): Boolean {
            return parentClass.isAssignableFrom(childClass)
        }
    }
}