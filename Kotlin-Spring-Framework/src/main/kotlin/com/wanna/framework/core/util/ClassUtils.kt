package com.wanna.framework.core.util

@Suppress("UNCHECKED_CAST")
class ClassUtils {
    companion object {
        /**
         * 判断childClass是否是parentClass的子类？
         */
        @JvmStatic
        fun isAssignFrom(parentClass: Class<*>, childClass: Class<*>): Boolean {
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
        @JvmStatic
        fun <T : Annotation> getAnnotationClassFromString(clazzName: String): Class<T> {
            return forName<Any>(clazzName) as Class<T>
        }

        @JvmStatic
        fun <T> forName(clazzName: String): Class<T> {
            return Class.forName(clazzName) as Class<T>
        }

        @JvmStatic
        fun <T> forName(clazzName: String, classLoader: ClassLoader): Class<T> {
            return Class.forName(clazzName, false, classLoader) as Class<T>
        }

        @JvmStatic
        fun <T> newInstance(clazz: Class<T>): T {
            return clazz.getDeclaredConstructor().newInstance()
        }

        @JvmStatic
        fun getDefaultClassLoader(): ClassLoader {
            var classLoader: ClassLoader? = null
            try {
                classLoader = Thread.currentThread().contextClassLoader
            } catch (ignored: Throwable) {

            }
            if (classLoader == null) {
                classLoader = ClassUtils::class.java.classLoader
            }
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader()
            }
            return classLoader!!
        }
    }
}