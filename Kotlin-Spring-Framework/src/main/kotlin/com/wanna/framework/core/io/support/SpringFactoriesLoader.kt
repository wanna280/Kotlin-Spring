package com.wanna.framework.core.io.support

import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.StringUtils
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个SpringFactories的加载工具类，负责完成spring.factories当中的配置文件的加载
 */
object SpringFactoriesLoader {
    private const val FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories"  // spring.factories文件的资源路径

    // 这是一个SpringFactories缓存，key-classLoader，hKey-factory，hValues-factoryNames
    private val cache = ConcurrentHashMap<ClassLoader, MutableMap<String, MutableList<String>>>()

    @JvmStatic
    fun <T> loadFactories(factoryType: Class<T>): MutableList<T> {
        return loadFactories(factoryType, null)
    }

    /**
     * 支持指定特定的类加载器去加载SpringFactories实例
     */
    @JvmStatic
    fun <T> loadFactories(factoryType: Class<T>, classLoader: ClassLoader?): MutableList<T> {
        val classLoaderToUse: ClassLoader = classLoader ?: SpringFactoriesLoader::class.java.classLoader
        // 根据类型加载该类型的所有的实现类的类名列表
        val factoryNames = LinkedHashSet(loadFactoryNames(factoryType))
        // 实例化所有的FactoryInstance
        val factoryInstances =
            createSpringFactoryInstances(factoryType, emptyArray(), classLoaderToUse, emptyArray(), factoryNames)

        // 完成所有的FactoryInstance的排序
        AnnotationAwareOrderComparator.sort(factoryInstances)
        return factoryInstances
    }

    /**
     * 创建SpringFactories
     *
     * @param factoryNames 要进行实例化的FactoryNames
     * @param type 类型，用来决定泛型
     * @param parameterTypes 构造器的参数类型列表
     * @param args 构造器参数列表
     */
    @JvmStatic
    fun <T> createSpringFactoryInstances(
        type: Class<T>,
        parameterTypes: Array<Class<*>>,
        classLoader: ClassLoader?,
        args: Array<Any>,
        factoryNames: Set<String>
    ): MutableList<T> {
        val classLoaderToUse: ClassLoader = classLoader ?: SpringFactoriesLoader::class.java.classLoader
        val instances = ArrayList<T>()
        try {
            factoryNames.forEach {
                val factoryType = ClassUtils.forName<T>(it, classLoaderToUse)
                val constructor = factoryType.getDeclaredConstructor(*parameterTypes)
                instances += BeanUtils.instantiateClass(constructor, *args)
            }
        } catch (ex: Exception) {
            throw IllegalArgumentException("通过构造器实例化FactoryInstance失败，原因是[ex=$ex]")
        }
        return instances
    }

    /**
     * 加载SpringFactoriesNames列表
     */
    @JvmStatic
    fun loadFactoryNames(factoryType: Class<*>): List<String> {
        return loadFactoryNames(factoryType, null)
    }

    /**
     * 给定具体的ClassLoader，去完成SpringFactories的加载
     *
     * @param classLoader 指定classLoader，如果为空，将会使用默认的classLoader
     */
    @JvmStatic
    fun loadFactoryNames(factoryType: Class<*>, classLoader: ClassLoader?): List<String> {
        val classLoaderToUse: ClassLoader = classLoader ?: SpringFactoriesLoader::class.java.classLoader
        return loadSpringFactories(classLoaderToUse)[factoryType.name] ?: emptyList()
    }

    /**
     * 给定具体的classLoader，去完成SpringFactories的加载
     */
    private fun loadSpringFactories(classLoader: ClassLoader): Map<String, List<String>> {
        var result = cache[classLoader]
        if (result != null) {
            return result
        }
        result = HashMap()

        try {
            val urls = classLoader.getResources(FACTORIES_RESOURCE_LOCATION)
            for (url in urls) {
                // 从给定的IOStream当中去加载Properties
                val properties = PropertiesLoaderUtils.loadProperties(url.openStream())

                properties.keys.forEach {
                    val factoryTypeName = (it as String).trim()  // 去掉首位
                    val factoryValues = properties[factoryTypeName].toString()
                    val factoryImplementNames = StringUtils.commaDelimitedListToStringArray(factoryValues)

                    // 将全部的factory的实现类都放入到列表当中去...
                    result.putIfAbsent(factoryTypeName, ArrayList())
                    factoryImplementNames.forEach { result[factoryTypeName]!! += it }
                }
            }
            // 将已经加载完成SpringFactories的结果加入到缓存当中
            cache[classLoader] = result
        } catch (ex: IOException) {
            throw IllegalStateException("无法加载从[location=$FACTORIES_RESOURCE_LOCATION]去加载到SpringFactories")
        }
        return result
    }
}