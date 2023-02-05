package com.wanna.framework.util

import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.constants.ANY_ARRAY_TYPE
import com.wanna.framework.constants.CLASS_ARRAY_TYPE
import com.wanna.framework.constants.NUMBER_ARRAY_TYPE
import com.wanna.framework.constants.STRING_ARRAY_TYPE
import com.wanna.framework.lang.Nullable
import java.lang.reflect.Method
import java.util.*

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
    const val DOT = "."

    /**
     * .class文件的后缀名
     */
    const val CLASS_FILE_SUFFIX = ".class"

    /**
     * CGLIB生成的代理类的分隔符
     */
    const val CGLIB_CLASS_SEPARATOR = "$$"

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
     * 非基础类型的数组前缀, 例如"[Ljava.lang.String;"
     */
    const val NON_PRIMITIVE_ARRAY_PREFIX = "[L"

    /**
     * 内层数组的前缀, 比如"[[I"或者是"[[java.lang.String;"这种情况
     */
    const val INTERNAL_ARRAY_PREFIX = "["

    /**
     * 内部类的分隔符
     */
    const val NESTED_CLASS_SEPARATOR = "$"

    /**
     * Logger
     */
    @JvmStatic
    private val logger = LoggerFactory.getLogger(ClassUtils::class.java)

    /**
     * Java的8种基础数据类型的"包装类->非包装类型"的映射关系
     */
    @JvmStatic
    private val primitiveWrapperTypeMap = LinkedHashMap<Class<*>, Class<*>>(8)

    /**
     * Java的8钟基础类型的"非包装类型->包装类型的映射关系"
     */
    @JvmStatic
    private val primitiveTypeToWrapperMap = LinkedHashMap<Class<*>, Class<*>>(8)

    /**
     * Java的8种基础数据类型的name->Type之间的映射关系, 例如"int"->"int.class", 当然也包含了对应的数组类型
     */
    @JvmStatic
    private val primitiveTypeNameMap = LinkedHashMap<String, Class<*>>(8)

    /**
     * 一些公共类的Cache
     */
    private val commonClassCache = LinkedHashMap<String, Class<*>>()

    init {

        // 初始化包装类->非包装类的映射关系(8种基础数据类型&Void)
        // Integer->int
        primitiveWrapperTypeMap[Int::class.javaObjectType] = Int::class.java
        // Double->double
        primitiveWrapperTypeMap[Double::class.javaObjectType] = Double::class.java
        // Boolean->boolean
        primitiveWrapperTypeMap[Boolean::class.javaObjectType] = Boolean::class.java
        // Float->float
        primitiveWrapperTypeMap[Float::class.javaObjectType] = Float::class.java
        // Byte->byte
        primitiveWrapperTypeMap[Byte::class.javaObjectType] = Byte::class.java
        // Long->long
        primitiveWrapperTypeMap[Long::class.javaObjectType] = Long::class.java
        // Short->short
        primitiveWrapperTypeMap[Short::class.javaObjectType] = Short::class.java
        // Char->char
        primitiveWrapperTypeMap[Char::class.javaObjectType] = Char::class.java
        // Void->void
        primitiveWrapperTypeMap[Void::class.javaObjectType] = Void::class.java

        // 初始化包装类->非包装类之间的映射关系
        primitiveWrapperTypeMap.forEach {
            primitiveTypeToWrapperMap[it.value] = it.key

            // register JavaObjectType to CommonClassCache
            registerCommonClasses(it.key)
        }

        // 统计出来所有的基础数据类型, 以及它的数组类型
        val primitiveTypes = LinkedHashSet(primitiveWrapperTypeMap.values)
        primitiveTypes.addAll(
            arrayOf(
                IntArray::class.java,
                DoubleArray::class.java,
                BooleanArray::class.java,
                FloatArray::class.java,
                ByteArray::class.java,
                LongArray::class.java,
                ShortArray::class.java,
                CharArray::class.java
            )
        )

        // 初始化基础数据类型的name->type的映射关系
        primitiveTypes.forEach { primitiveTypeNameMap[it.name] = it }

        // 注册基础数据类型数组, 比如Integer[]注册到CommonClassCache当中
        registerCommonClasses(
            Array<Boolean?>::class.java,
            Array<Byte?>::class.java,
            Array<Char?>::class.java,
            Array<Double?>::class.java,
            Array<Float?>::class.java,
            Array<Int?>::class.java,
            Array<Long?>::class.java,
            Array<Short?>::class.java
        )
        // 注册Number/Array<Number>/String/Array<String>/Class/Array<Class>/Object/Array<Object>
        registerCommonClasses(
            Number::class.java,
            NUMBER_ARRAY_TYPE::class.java,
            String::class.java,
            STRING_ARRAY_TYPE::class.java,
            Class::class.java,
            CLASS_ARRAY_TYPE::class.java,
            Any::class.java,
            ANY_ARRAY_TYPE::class.java
        )

        // 注册异常相关的公共类
        registerCommonClasses(
            Throwable::class.java,
            Exception::class.java,
            RuntimeException::class.java,
            Error::class.java,
            StackTraceElement::class.java,
            Array<StackTraceElement>::class.java
        )

        // 注册迭代相关的基础类
        registerCommonClasses(
            Enum::class.java,
            Iterable::class.java,
            MutableIterator::class.java,
            Enumeration::class.java,
            MutableCollection::class.java,
            MutableList::class.java,
            MutableSet::class.java,
            MutableMap::class.java,
            MutableMap.MutableEntry::class.java,
            Optional::class.java
        )
    }

    /**
     * 将给定的这些类去注册到commonClassesCache这个缓存当中去(提供对于类的解析时的快速获取)
     *
     * @param commonClasses 要去进行注册的commonClasses
     */
    private fun registerCommonClasses(vararg commonClasses: Class<*>) {
        commonClasses.forEach { commonClassCache[it.name] = it }
    }

    /**
     * 根据基础数据类型的name去获取到对应的基础数据类型的Class
     *
     * @param name (int/float/double/long/...)
     * @return 如果name是一个基础数据类型, 返回对应的基础数据类型Class; 否则return null
     */
    @JvmStatic
    @Nullable
    fun resolvePrimitiveClassName(@Nullable name: String?): Class<*>? {
        var result: Class<*>? = null
        // 因为对于大多数的类都会很长, 因为它们都被放到一个包当中,
        // 但是基础数据类型没有包(最长是boolean, 长度为7), 因此对于包的长度检验是很有价值的...
        if (name != null && name.length <= 7) {
            result = primitiveTypeNameMap[name]
        }
        return result
    }

    /**
     * 如果给定的类型是基础数据类型的话, 那么返回它的包装类, 否则返回原始的类型
     *
     * @param clazz 原始的Class
     * @return 经过解析之后的Class
     */
    @JvmStatic
    fun resolvePrimitiveIfNecessary(clazz: Class<*>): Class<*> {
        return if (clazz.isPrimitive && clazz != Unit::class.java) primitiveTypeToWrapperMap[clazz]!! else clazz
    }

    /**
     * 判断childClass是否是parentClass的子类? 如果其中一个返回为空, 那么return true; 只有两者均不为空时, 才会去进行判断
     *
     * @param parentClass parentClass
     * @param childClass parentClass
     * @see isAssignFrom
     */
    @JvmStatic
    fun isAssignFrom(@Nullable parentClass: Class<*>?, @Nullable childClass: Class<*>?): Boolean =
        isAssignable(parentClass, childClass)

    /**
     * 检查给定的value, 能否被cast为给定的类型
     *
     * @param type type
     * @param value 实例对象
     * @return 如果value可以被cast成为type, 那么return true; 否则return false
     */
    @JvmStatic
    fun isAssignableValue(type: Class<*>, @Nullable value: Any?): Boolean =
        if (value != null) isAssignFrom(type, value::class.java) else !type.isPrimitive

    /**
     * 判断childClass是否是parentClass的子类? 如果其中一个返回为空, 那么return true; 只有两者均不为空时, 才会去进行判断
     *
     * @param lhsType parentClass
     * @param rhsType childClass
     * @return 如果childClass可以转换为parentClass, 那么return true; 否则return false
     */
    @Nullable
    fun isAssignable(@Nullable lhsType: Class<*>?, @Nullable rhsType: Class<*>?): Boolean {
        if (lhsType == null || rhsType == null) {
            return false
        }
        // 如果类型直接就能去进行转换的话, 那么return true
        if (lhsType.isAssignableFrom(rhsType)) {
            return true
        }
        // 如果parent是基础数据类型的话
        if (lhsType.isPrimitive) {
            // 如果parentClass是基础类型的话, 那么看childClass是否是它的包装类?
            return primitiveWrapperTypeMap[rhsType] == lhsType
        }
        // 如果parentClass不是基础数据类型, 但是childClass为基础数据类型的话, 那么拿出来childClass的包装类, 和parentClass去匹配...
        // 例如parentClass=Number.class, childClass是Int.class这种情况, 是该return true的
        val wrapper = primitiveTypeToWrapperMap[rhsType]
        return wrapper != null && lhsType.isAssignableFrom(wrapper)
    }

    /**
     * 获取一个短的类名, 也就是一个类的去掉包名之后的类名
     * 比如：
     * * 1."com.wanna.User"会被转换为"User",
     * * 2."com.wanna.User$Default"会被转换为"User$Default"
     *
     * @param clazz 想要获取短类名的clazz
     * @return 解析完成的短类名
     */
    @JvmStatic
    fun getShortName(clazz: Class<*>): String = getShortName(clazz.name)

    /**
     * 获取一个短的类名, 也就是一个类的去掉包名之后的类名
     * 比如：
     * * 1."com.wanna.User"会被转换为"User",
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
     * 根据className, 获取到AnnotationClass
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
     * 使用Class.forName的方式去, 获取到Class(使用默认的ClassLoader)
     *
     * @param clazzName className
     * @return 加载到的类
     */
    @JvmStatic
    fun <T> forName(clazzName: String): Class<T> {
        return forName(clazzName, null)
    }

    /**
     * 使用Class.forName的方式去, 获取到Class(可以使用自定义的ClassLoader)
     *
     * Note: 支持各种各样的数组风格, 也支持基于基础数据类型的Class的获取
     *
     * @param clazzName className
     * @param classLoader 要使用的ClassLoader
     * @return 根据给定的className, 去获取到的类Class对象
     * @throws ClassNotFoundException 如果给定的className对应的类无法找到
     */
    @Throws(ClassNotFoundException::class)
    @JvmStatic
    fun <T> forName(clazzName: String, @Nullable classLoader: ClassLoader?): Class<T> {

        // 尝试从基础数据类型/commonClass当中去获取
        val clazz = resolvePrimitiveClassName(clazzName) ?: commonClassCache[clazzName]
        // 如果是一些commonClass的话, 那么直接return
        if (clazz != null) {
            return clazz as Class<T>
        }
        // 如果它是以"[]"作为后缀的话, 说明它是数组类型话, 那么需要解析它的elementType去进行递归forName
        if (clazzName.endsWith(ARRAY_SUFFIX)) {
            val elementClassName = clazzName.substring(0, clazzName.length - ARRAY_SUFFIX.length)
            val elementClass = forName<Any>(elementClassName, classLoader)
            return java.lang.reflect.Array.newInstance(elementClass, 0).javaClass as Class<T>
        }
        // 如果是"[Ljava.lang.String;"这种风格的数组的话, 那么也需要解析它的elementType去进行递归forName
        if (clazzName.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && clazzName.endsWith(";")) {
            val elementClassName = clazzName.substring(NON_PRIMITIVE_ARRAY_PREFIX.length, clazzName.length - 1)
            val elementClass = forName<Any>(elementClassName, classLoader)
            return java.lang.reflect.Array.newInstance(elementClass, 0).javaClass as Class<T>
        }

        // 如果不是以"[]"作为后缀, 也不是以"[L"作为前缀, 但是是以"["作为前缀的话, 它可能是一个多层的数组, 也需要递归解析元素类型
        if (clazzName.startsWith(INTERNAL_ARRAY_PREFIX)) {
            val elementClassName = clazzName.substring(INTERNAL_ARRAY_PREFIX.length)
            val elementClass = forName<Any>(elementClassName, classLoader)
            return java.lang.reflect.Array.newInstance(elementClass, 0).javaClass as Class<T>
        }

        // 如果不是数组的话, 那么我们支持
        val classLoaderToUse = classLoader ?: getDefaultClassLoader()
        try {
            return Class.forName(clazzName, false, classLoaderToUse) as Class<T>
        } catch (ex: ClassNotFoundException) {
            // 如果丢出来异常的话, 那么继续尝试解析一下看它是否是内部类的情况, 但是写成了"."的方式去进行分割的className
            // 比如com.wanna.UserInfo的内部类有一个User, 那么支持fallback去使用com.wanna.UserInfo.User的方式去获取到这个内部类
            val dotIndex = clazzName.lastIndexOf(DOT)
            if (dotIndex != -1) {
                // 如果是
                val nestedClassName =
                    clazzName.substring(0, dotIndex) + NESTED_CLASS_SEPARATOR + clazzName.substring(dotIndex + 1)
                try {
                    return Class.forName(nestedClassName, false, classLoader) as Class<T>  // use Class.forName
                } catch (ex: ClassNotFoundException) {
                    // 对于解析内部类的fallback的情况, 我们不进行处理, 别把外层的原始异常吃掉了... 需要直接把原始的异常丢出去
                }
            }
            if (logger.isTraceEnabled) {
                logger.trace("Cannot find Class for name: $clazzName, classLoader=$classLoaderToUse")
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
     * 判断指定的类是否存在于当前JVM的运行时的依赖当中?
     *
     * @param className 要去进行判断的className
     * @return 存在return true; 不存在return false
     */
    @JvmStatic
    fun isPresent(className: String): Boolean {
        return try {
            forName<Any>(className, null)
            true
        } catch (ex: IllegalAccessException) {
            throw IllegalStateException(
                "Readability mismatch in inheritance hierarchy of class [$className], message:${ex.message}", ex
            )
        } catch (ex: Throwable) {
            // NoClassDefException/ClassNotFoundException
            return false
        }
    }

    /**
     * 判断指定的类当中是否存在有给定名字的方法?
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
     * 获取目标类当中的给定的name的public方法(并且参数名也完全匹配)
     *
     * @param clazz clazz
     * @param methodName methodName
     * @param paramTypes parameterTypes
     * @return 如果存在这样的方法, 那么return 该方法; 否则return null
     */
    @Nullable
    @JvmStatic
    fun getMethodOrNull(clazz: Class<*>, methodName: String, paramTypes: Array<Class<*>>): Method? {
        try {
            return clazz.getMethod(methodName, *paramTypes)
        } catch (ex: Throwable) {
            return null
        }
    }

    /**
     * 获取给定的类上的给定methodName的方法
     *
     * @param clazz clazz
     * @param methodName methodName
     * @param paramTypes paramTypes
     * @return Method(如果无法找到, 或者是数量不为1的话, 那么return null)
     */
    @Nullable
    @JvmStatic
    fun getMethodIfAvailable(clazz: Class<*>, methodName: String, @Nullable paramTypes: Array<Class<*>>?): Method? {
        if (paramTypes != null) {
            return getMethodOrNull(clazz, methodName, paramTypes)
        } else {
            val methods = findMethodCandidatesByName(clazz, methodName)
            return if (methods.size == 1) methods.iterator().next() else null
        }
    }

    /**
     * 根据方法名去某个类当中去找到所有匹配的方法列表
     *
     * @param clazz 要去匹配的类
     * @param methodName 要寻找方法的方法名
     * @return 方法名符合的Method列表(找不到的话, return empty)
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
     * 判断指定的类是否存在于当前JVM的运行时的依赖当中?
     *
     * @param className 要去进行判断的className
     * @param classLoader 要使用的ClassLoader
     * @return 存在return true; 不存在return false
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
     * 获取一个方法的全限定名, 格式为"类名.方法名"
     *
     * @param method method
     * @param clazz clazz(如果为null, 将会使用method.declaringClass作为clazz)
     * @return 该方法的全限定名
     */
    @JvmStatic
    fun getQualifiedMethodName(method: Method, @Nullable clazz: Class<*>?): String {
        return (clazz ?: method.declaringClass).name + "." + method.name
    }

    /**
     * 获取一个Class的文件名(简单类名+".class"), 例如String.class
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
     * 获取指定的类的包名
     *
     * @param clazz clazz
     * @return packageName
     */
    @JvmStatic
    fun getPackageName(clazz: Class<*>): String = getPackageName(clazz.name)

    /**
     * 指定一个className, 获取它的包名
     *
     * @param fullQualifierName 类的全类名
     * @return 解析到的包名(切取最后一个'.'之前的部分去作为packageName)
     */
    @JvmStatic
    fun getPackageName(fullQualifierName: String): String {
        val lastIndex = fullQualifierName.lastIndexOf(DOT)
        return if (lastIndex == -1) fullQualifierName else fullQualifierName.substring(0, lastIndex)
    }

    /**
     * 获取一个类的全部父接口, 并以Set的方式去进行返回
     *
     * @param clazz 要获取接口的目标类
     * @return 该类的所有接口(Set)
     */
    @JvmStatic
    fun getAllInterfacesForClassAsSet(clazz: Class<*>): Set<Class<*>> {
        val interfaces = LinkedHashSet<Class<*>>()
        // 如果它是接口的话, 直接return
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
     * 给定一个类, 去生成这个类的全限定名;
     * 如果这个类是一个数组类, 那么需要加上后缀"[]";
     * 如果一个类不是一个数组类, 那么就是正常返回一个类的className
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
     * 将资源名转换成为类名, 将资源路径当中的"/"去转换成为"."
     *
     * @param resourcePath 资源路径
     * @return className
     */
    @JvmStatic
    fun convertResourcePathToClassName(resourcePath: String): String =
        resourcePath.replace(PATH_SEPARATOR, PACKAGE_SEPARATOR)

    /**
     * 将类名转换成为资源名, 将包名当中的"."去替换成为"/"
     *
     * @param className className
     * @return 资源路径
     */
    @JvmStatic
    fun convertClassNameToResourcePath(className: String): String = className.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR)

    /**
     * 获取目标对象的用户定义的类型, 因为有些对象是被CGLIB生成的, 因此我们有可能需要获取没有被CGLIB代理之前它的原始的类
     *
     * @param value Java对象
     * @return 该Java对象对应的原始的类
     */
    @JvmStatic
    fun getUserClass(value: Any): Class<*> = getUserClass(value::class.java)

    /**
     * 获取目标类的用户定义的类型, 因为有些类是被CGLIB生成的, 因此我们有可能需要获取没有被CGLIB代理之前它的原始的类
     *
     * @param clazz 原始的类(可能被CGLIB代理过)
     * @return 解析出来的没有被CGLIB代理之前的用户定义的类
     */
    @JvmStatic
    fun getUserClass(clazz: Class<*>): Class<*> {
        // 如果类名当中含有"$$", 那么就说明它是被CGLIB代理过的类
        if (clazz.name.contains(CGLIB_CLASS_SEPARATOR)) {
            val superclass = clazz.superclass
            if (superclass != null && superclass != Any::class.java) {
                return superclass
            }
        }
        return clazz
    }
}