package com.wanna.framework.core.io.support

import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个SpringFactories的加载工具类, 负责完成spring.factories当中的配置文件的加载
 */
object SpringFactoriesLoader {

    /**
     * spring.factories文件的资源路径
     */
    private const val FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories"

    /**
     * 这是一个SpringFactories缓存, Key-classLoader, HKey-factoryType, HValues-factoryNames
     */
    @JvmStatic
    private val cache = ConcurrentHashMap<ClassLoader, MutableMap<String, MutableList<String>>>()

    /**
     * 从SpringFactories当中去加载到给定的[factoryType]的类型的全部实例对象
     *
     * @param factoryType factoryType
     * @return 加载到的该类型的全部实例对象
     */
    @JvmStatic
    fun <T : Any> loadFactories(factoryType: Class<T>): MutableList<T> = loadFactories(factoryType, null)

    /**
     * 支持指定的类加载器去加载得到SpringFactories实例
     *
     * @param factoryType 要去进行加载的Factory类型
     * @param classLoader 要使用的ClassLoader
     * @return 加载得到的实例对象列表
     */
    @JvmStatic
    fun <T : Any> loadFactories(factoryType: Class<T>, @Nullable classLoader: ClassLoader?): MutableList<T> {
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
     * @param type 类型, 用来决定泛型
     * @param parameterTypes 构造器的参数类型列表
     * @param args 构造器参数列表
     *
     * @param T 实例化的Bean的类型, 通过type参数去进行推断
     */
    @JvmStatic
    fun <T : Any> createSpringFactoryInstances(
        type: Class<T>,
        parameterTypes: Array<Class<*>>,
        classLoader: ClassLoader?,
        args: Array<Any>,
        factoryNames: Set<String>
    ): MutableList<T> {
        val classLoaderToUse: ClassLoader = classLoader ?: SpringFactoriesLoader::class.java.classLoader
        val factoryInstances = ArrayList<T>()
        for (factoryName in factoryNames) {
            try {
                val factoryType = ClassUtils.forName<T>(factoryName, classLoaderToUse)
                val constructor = factoryType.getDeclaredConstructor(*parameterTypes)
                constructor.isAccessible = true
                factoryInstances += constructor.newInstance(*args)
            } catch (ex: Exception) {
                throw IllegalArgumentException("Instantiate factory [$factoryName] instance error", ex)
            }
        }
        return factoryInstances
    }

    /**
     * 加载SpringFactoriesNames列表
     */
    @JvmStatic
    fun loadFactoryNames(factoryType: Class<*>): List<String> {
        return loadFactoryNames(factoryType, null)
    }

    /**
     * 给定具体的ClassLoader, 去完成SpringFactories的加载
     *
     * @param classLoader 指定classLoader, 如果为空, 将会使用默认的classLoader
     */
    @JvmStatic
    fun loadFactoryNames(factoryType: Class<*>, classLoader: ClassLoader?): List<String> {
        val classLoaderToUse: ClassLoader = classLoader ?: SpringFactoriesLoader::class.java.classLoader
        return loadSpringFactories(classLoaderToUse)[factoryType.name] ?: emptyList()
    }

    /**
     * 给定具体的classLoader, 去完成SpringFactories的加载
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
                val properties = PropertiesLoaderUtils.loadPropertiesFromProperties(url.openStream())

                properties.keys.forEach {
                    val factoryTypeName = (it as String).trim()  // 去掉首位
                    val factoryValues = properties[factoryTypeName].toString()
                    val factoryImplementNames = StringUtils.commaDelimitedListToStringArray(factoryValues)

                    // 将全部的factory的实现类都放入到列表当中去...
                    result.putIfAbsent(factoryTypeName, ArrayList())
                    factoryImplementNames.filter { it.isNotBlank() }.forEach { result[factoryTypeName]!! += it }
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