package com.wanna.framework.core.util

/**
 * 这是一个Class的相关工具类
 */
@Suppress("UNCHECKED_CAST")
object ClassUtils {
    /**
     * 判断childClass是否是parentClass的子类？如果其中一个返回为空，那么return true；只有两者均不为空时，才会去进行判断
     *
     * @param parentClass 父类
     * @param childClass 子类
     */
    @JvmStatic
    fun isAssignFrom(parentClass: Class<*>?, childClass: Class<*>?): Boolean {
        return if (parentClass != null && childClass != null) parentClass.isAssignableFrom(childClass) else false
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