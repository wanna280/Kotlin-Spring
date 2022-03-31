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

        /**
         * 获取一个类的去掉包名之后的类名
         */
        @JvmStatic
        fun getShortName(clazz: Class<*>): String {
            return getShortName(clazz.name)
        }

        @JvmStatic
        fun getShortName(clazzName: String): String {
            val lastDotIndex = clazzName.lastIndexOf(".")
            return clazzName.substring(lastDotIndex + 1)
        }

        /**
         * 根据className，获取到AnnotationClass
         */
        fun <T : Annotation> getAnnotationClassFromString(clazzName: String): Class<T> {
            return forName<Any>(clazzName) as Class<T>
        }

        fun <T> forName(clazzName: String): Class<T> {
            return Class.forName(clazzName) as Class<T>
        }

        fun <T> newInstance(clazz: Class<T>): T {
            return clazz.getDeclaredConstructor().newInstance()
        }
    }
}