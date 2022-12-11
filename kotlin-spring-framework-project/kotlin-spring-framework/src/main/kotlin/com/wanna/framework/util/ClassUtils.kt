package com.wanna.framework.util

import com.wanna.framework.lang.Nullable
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * Spring当中对于Class相关操作的工具类封装
 *
 * @author jianchao.jia
 */
@Suppress("UNCHECKED_CAST")
object ClassUtils {

    /**
     * "."的常量
     */
    private const val DOT = "."

    /**
     * .class文件的后缀名
     */
    private const val CLASS_FILE_SUFFIX = ".class"

    /**
     * 包分隔符
     */
    const val PACKAGE_SEPARATOR = "."

    /**
     * 路径的分隔符
     */
    const val PATH_SEPARATOR = "/"

    /**
     * 数组的全限定名后缀
     */
    const val ARRAY_SUFFIX = "[]"

    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(ClassUtils::class.java)

    /**
     * 判断childClass是否是parentClass的子类？如果其中一个返回为空，那么return true；只有两者均不为空时，才会去进行判断
     *
     * @param parentClass 父类
     * @param childClass 子类
     */
    @JvmStatic
    fun isAssignFrom(@Nullable parentClass: Class<*>?, @Nullable childClass: Class<*>?): Boolean {
        return if (parentClass != null && childClass != null) parentClass.isAssignableFrom(childClass) else false
    }

    /**
     * 获取一个短的类名，也就是一个类的去掉包名之后的类名
     * 比如：
     * * 1."com.wanna.User"会被转换为"User"，
     * * 2."com.wanna.User$Default"会被转换为"User$Default"
     *
     * @param clazz 想要获取短类名的clazz
     * @return 解析完成的短类名
     */
    @JvmStatic
    fun getShortName(clazz: Class<*>): String {
        return getShortName(clazz.name)
    }

    /**
     * 获取一个短的类名，也就是一个类的去掉包名之后的类名
     * 比如：
     * * 1."com.wanna.User"会被转换为"User"，
     * * 2."com.wanna.User$Default"会被转换为"User$Default"
     *
     * @param clazzName 想要获取短类名的className
     * @return 解析完成的短类名
     */
    @JvmStatic
    fun getShortName(clazzName: String): String {
        val lastDotIndex = clazzName.lastIndexOf(DOT)
        return clazzName.substring(lastDotIndex + 1)
    }

    /**
     * 根据className，获取到AnnotationClass
     *
     * @param clazzName className
     * @param T 需要返回的注解类型
     * @return Class.forName得到的AnnotationClass
     */
    @JvmStatic
    fun <T : Annotation> getAnnotationClassFromString(clazzName: String): Class<T> {
        return forName<Any>(clazzName) as Class<T>
    }

    /**
     * 使用Class.forName的方式去，获取到Class(使用默认的ClassLoader)
     *
     * @param clazzName className
     * @return 加载到的类
     */
    @JvmStatic
    fun <T> forName(clazzName: String): Class<T> {
        return forName(clazzName, null)
    }

    /**
     * 使用Class.forName的方式去，获取到Class(可以使用自定义的ClassLoader)
     *
     * @param clazzName className
     * @param classLoader 要使用的ClassLoader
     * @return 加载到的类
     */
    @Throws(ClassNotFoundException::class)
    @JvmStatic
    fun <T> forName(clazzName: String, @Nullable classLoader: ClassLoader?): Class<T> {
        val classLoaderToUse = classLoader ?: getDefaultClassLoader()
        try {
            return Class.forName(clazzName, false, classLoaderToUse) as Class<T>
        } catch (ex: ClassNotFoundException) {
            if (logger.isTraceEnabled) {
                logger.trace("无法从当前JVM的依赖当中去解析到给定的className=[$clazzName]的类")
            }
            throw ex
        }
    }

    /**
     * 解析className成为一个Class
     *
     * @param clazzName clazzName
     * @param classLoader Class.forName使用到的ClassLoader
     * @throws IllegalArgumentException 如果无法解析给定的类的话
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun resolveClassName(clazzName: String, @Nullable classLoader: ClassLoader?): Class<*> {
        try {
            return forName<Any>(clazzName, classLoader)
        } catch (ex: ClassNotFoundException) {
            throw IllegalArgumentException("无法找到给定的类[$clazzName]")
        } catch (ex: LinkageError) {
            throw IllegalArgumentException("无法在运行时去链接到给定的类[$clazzName]")
        }
    }

    /**
     * 判断指定的类是否存在于当前JVM的运行时的依赖当中？
     *
     * @param className 要去进行判断的className
     * @return 存在return true；不存在return false
     */
    @JvmStatic
    fun isPresent(className: String): Boolean {
        return try {
            forName<Any>(className, null)
            true
        } catch (ex: ClassNotFoundException) {
            return false
        }
    }

    /**
     * 判断指定的类当中是否存在有给定名字的方法？
     * (Note: 1.只找public方法)
     *
     * @param clazz clazz
     * @param methodName 方法名
     */
    @JvmStatic
    fun hasMethod(clazz: Class<*>, methodName: String): Boolean {
        val candidates = findMethodCandidatesByName(clazz, methodName)
        return candidates.size == 1
    }

    /**
     * 根据方法名去某个类当中去找到所有匹配的方法列表
     *
     * @param clazz 要去匹配的类
     * @param methodName 要寻找方法的方法名
     * @return 方法名符合的Method列表(找不到的话，return empty)
     */
    private fun findMethodCandidatesByName(clazz: Class<*>, methodName: String): Set<Method> {
        val candidates = LinkedHashSet<Method>()
        clazz.methods.forEach {
            if (it.name == methodName) {
                candidates += it
            }
        }
        return candidates
    }

    /**
     * 判断指定的类是否存在于当前JVM的运行时的依赖当中？
     *
     * @param className 要去进行判断的className
     * @param classLoader 要使用的ClassLoader
     * @return 存在return true；不存在return false
     */
    @JvmStatic
    fun isPresent(className: String, @Nullable classLoader: ClassLoader? = null): Boolean {
        return try {
            forName<Any>(className, classLoader)
            true
        } catch (ex: ClassNotFoundException) {
            return false
        }
    }

    /**
     * 使用指定的类的无参数构造器去实例化一个对象
     *
     * @param clazz 想要去进行实例化的类
     * @return 实例化完成的Java对象
     */
    @JvmStatic
    fun <T> newInstance(clazz: Class<T>): T {
        return clazz.getDeclaredConstructor().newInstance()
    }

    /**
     * 获取默认的ClassLoader
     *
     * @return 默认的ClassLoader
     */
    @JvmStatic
    fun getDefaultClassLoader(): ClassLoader {
        var classLoader: ClassLoader? = null

        // 1.最优先考虑使用线程的类加载器
        try {
            classLoader = Thread.currentThread().contextClassLoader
        } catch (ignored: Throwable) {

        }

        // 2.其次考虑ClassUtils类的类加载器
        if (classLoader == null) {
            classLoader = ClassUtils::class.java.classLoader
        }

        // 3.之后考虑SystemClassLoader
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader()
        }
        return classLoader!!
    }

    /**
     * 获取一个方法的全限定名，格式为"类名.方法名"
     *
     * @param method method
     * @param clazz clazz(如果为null，将会使用method.declaringClass作为clazz)
     * @return 该方法的全限定名
     */
    @JvmStatic
    fun getQualifiedMethodName(method: Method, @Nullable clazz: Class<*>?): String {
        return (clazz ?: method.declaringClass).name + "." + method.name
    }

    /**
     * 获取一个Class的文件名(简单类名+".class")，例如String.class
     *
     * @param clazz class
     * @return ClassFileName
     */
    @JvmStatic
    fun getClassFileName(clazz: Class<*>): String {
        val lastIndexOfDot = clazz.name.lastIndexOf(DOT)
        return clazz.name.substring(lastIndexOfDot + 1) + CLASS_FILE_SUFFIX
    }

    /**
     * 指定一个className，获取它的包名
     *
     * @param fullQualifierName 类的全类名
     * @return 解析到的包名
     */
    @JvmStatic
    fun getPackageName(fullQualifierName: String): String {
        val lastIndex = fullQualifierName.lastIndexOf(DOT)
        return if (lastIndex == -1) fullQualifierName else fullQualifierName.substring(0, lastIndex)
    }

    /**
     * 获取一个类的全部父接口，并以Set的方式去进行返回
     *
     * @param clazz 要获取接口的目标类
     * @return 该类的所有接口(Set)
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
            interfaces += clazz.interfaces
            current = current.superclass
        }
        return interfaces
    }

    /**
     * 获取一个类的全部子接口作为Array<Class<*>>
     *
     * @param clazz 要去获取接口的类
     * @return 目标类的所有接口(以Array的方式去进行返回)
     */
    @JvmStatic
    fun getAllInterfacesForClass(clazz: Class<*>): Array<Class<*>> {
        return getAllInterfacesForClassAsSet(clazz).toList().toTypedArray()
    }

    /**
     * 将类名转换成为资源名，将包名当中的"."去替换成为"/"
     *
     * @param className className
     * @return 资源路径
     */
    @JvmStatic
    fun convertClassNameToResourcePath(className: String): String = className.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR)

    /**
     * 给定一个类，去生成这个类的全限定名；
     * 如果这个类是一个数组类，那么需要加上后缀"[]"；
     * 如果一个类不是一个数组类，那么就是正常返回一个类的className
     *
     * @param clazz clazz
     * @return 全限定名字符串
     */
    @JvmStatic
    fun getQualifiedName(clazz: Class<*>): String = if (clazz.isArray) getQualifiedNameForArray(clazz) else clazz.name

    /**
     * 从一个数组的Class当中去获取它的全限定名
     *
     * @param clazz 数组的Class
     * @return 数组的全限定名
     */
    @JvmStatic
    private fun getQualifiedNameForArray(clazz: Class<*>): String {
        val builder = StringBuilder()
        var clazzToUse = clazz
        while (clazzToUse.isArray) {
            clazzToUse = clazzToUse.componentType
            builder.append(ARRAY_SUFFIX)
        }
        builder.insert(0, clazzToUse.name)  // insert before
        return builder.toString()
    }

    /**
     * 将资源名转换成为类名，将资源路径当中的"/"去转换成为"."
     *
     * @param resourcePath 资源路径
     * @return className
     */
    fun convertResourcePathToClassName(resourcePath: String): String =
        resourcePath.replace(PATH_SEPARATOR, PACKAGE_SEPARATOR)
}