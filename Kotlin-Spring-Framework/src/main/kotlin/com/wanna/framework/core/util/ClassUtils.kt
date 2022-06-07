package com.wanna.framework.core.util

/**
 * 这是一个Class的相关工具类
 */
@Suppress("UNCHECKED_CAST")
object ClassUtils {

    private const val DOT = "."
    private const val CLASS_FILE_SUFFIX = ".class"

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
        val lastDotIndex = clazzName.lastIndexOf(DOT)
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
    fun <T> forName(clazzName: String, classLoader: ClassLoader?): Class<T> {
        val classLoaderToUse = classLoader ?: ClassUtils::class.java.classLoader
        return Class.forName(clazzName, false, classLoaderToUse) as Class<T>
    }

    /**
     * 判断指定的类是否存在？
     *
     * @return 存在return true；不存在return false
     */
    @JvmStatic
    fun isPresent(className: String, classLoader: ClassLoader): Boolean {
        return try {
            forName<Any>(className, classLoader)
            true
        } catch (ex: ClassNotFoundException) {
            return false
        }
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

    /**
     * 获取一个Class的文件名(简单类名+".class")，例如String.class
     */
    @JvmStatic
    fun getClassFileName(clazz: Class<*>): String {
        val lastIndexOfDot = clazz.name.lastIndexOf(DOT)
        return clazz.name.substring(lastIndexOfDot + 1) + CLASS_FILE_SUFFIX
    }

    /**
     * 获取一个类的全部父接口
     *
     * @param clazz 目标类
     */
    @JvmStatic
    fun getAllInterfacesForClassAsSet(clazz: Class<*>): Set<Class<*>> {
        val interfaces = LinkedHashSet<Class<*>>()

        // 如果它是接口的话，直接return
        if (clazz.isInterface) {
            return setOf(clazz)
        }
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            val itfs = clazz.interfaces
            interfaces += itfs
            current = current.superclass
        }

        return interfaces
    }
}