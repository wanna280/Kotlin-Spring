package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive
import com.wanna.boot.loader.archive.ExplodedArchive
import com.wanna.boot.loader.util.SystemPropertiesUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import javax.annotation.Nullable

/**
 * 基于Properties配置文件的方式去对应用去进行启动的[Launcher]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/12
 */
open class PropertiesLauncher : Launcher() {

    companion object {

        /**
         * 只是存在有parentClassLoader的参数的参数类型列表
         */
        @JvmStatic
        private val PARENT_ONLY_PARAMS: Array<Class<*>> = arrayOf(ClassLoader::class.java)

        /**
         * 有URLs和parentClassLoader的参数的参数类型列表
         */
        @JvmStatic
        private val URLS_AND_PARENT_PARAMS = arrayOf(Array<URL>::class.java, ClassLoader::class.java)

        /**
         * 空URL数组
         */
        @JvmStatic
        private val NO_URLS = emptyArray<URL>()

        /**
         * 为参数的参数类型列表
         */
        @JvmStatic
        private val NO_PARAMS = emptyArray<Class<*>>()

        /**
         * 当前是否是Debug程序
         */
        private const val DEBUG = "loader.debug"

        /**
         * 主启动类的属性Key, 也可以通过Manifest的"Start-Class"去进行指定
         */
        const val MAIN = "loader.main"

        /**
         * Loader要使用的ClassLoader
         */
        private const val LOADER_CLASSLOADER = "loader.classLoader"

        /**
         * Loader要额外使用的参数列表(多个参数之间使用','去进行分隔)
         */
        private const val LOADER_ARGS = "loader.args"

        /**
         * Loader的Home目录(默认使用"user.dir"去作为Home目录)
         */
        private const val LOADER_HOME = "loader.home"

        /**
         * Loader的配置文件路径
         */
        private const val CONFIG_LOCATION = "loader.config.location"

        /**
         * Loader的配置文件属性名
         */
        private const val CONFIG_NAME = "loader.config.name"

        /**
         * Manifest当中的Start-Class
         */
        private const val MANIFEST_KEY_START_CLASS = "Start-Class"

        /**
         * Word分隔符
         */
        @JvmStatic
        private val WORD_SEPARATOR = Pattern.compile("\\W+")


        @JvmStatic
        fun main(vararg args: String) {
            val propertiesLauncher = PropertiesLauncher()
            propertiesLauncher.launch(arrayOf(*args))
        }
    }

    /**
     * HomeDirectory, 可以通过"loader.home"去进行指定, 默认为"user.dir"用户家目录
     */
    private val home: File

    /**
     * 存放相关配置信息的Properties
     */
    private val properties = Properties()

    private val parent: Archive


    init {
        try {
            this.home = this.getHomeDirectory()
            initializeProperties()
            initializePaths()
            this.parent = super.createArchive()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
    }

    /**
     * 获取引导应用启动的主类, 优先从"loader.main"属性当中去进行获取, 获取不到的话, 从Manifest的"Start-Class"当中去进行获取
     *
     * @return mainClass
     */
    override fun getMainClass(): String {
        return getProperty(MAIN, MANIFEST_KEY_START_CLASS)
            ?: throw IllegalStateException("No '$MAIN' or '$MANIFEST_KEY_START_CLASS' specified")
    }

    /**
     * 创建[ClassLoader], 用该[ClassLoader]去加载主启动类, 去完成应用程序的启动
     *
     * @param archives Archives
     * @return ClassLoader
     */
    override fun createClassLoader(archives: Iterator<Archive>): ClassLoader {
        val classLoaderName = getProperty(LOADER_CLASSLOADER) ?: return super.createClassLoader(archives)
        val urls = LinkedHashSet<URL>()
        for (archive in archives) {
            urls += archive.getUrl()
        }
        val loader = LaunchedURLClassLoader(urls.toTypedArray(), javaClass.classLoader)
        debug("Classpath for custom loaders: $urls")

        val customClassLoader = wrapWithCustomClassLoader(loader, classLoaderName)
        debug("Using custom class loader: $classLoaderName")
        return customClassLoader
    }

    override fun getClassPathArchivesIterator(): Iterator<Archive> {
        TODO("Not yet implemented")
    }


    /**
     * 获取到Loader的Home目录
     *
     * * 1.如果有指定"loader.home"系统属性的话, 那么使用该属性值去作为HomeDirectory
     * * 2.如果没有指定"loader.home"系统属性的话, 那么使用"user.dir"属性值(用户家目录)去作为HomeDirectory
     *
     * @return HomeDirectory
     */
    protected open fun getHomeDirectory(): File {
        try {
            val homeDir = getPropertyWithDefault(LOADER_HOME, "${'$'}{user.dir}")!!
            return File(homeDir)
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
    }

    private fun initializePaths() {

    }


    /**
     * 使用自定义的ClassLoader去进行包装原始的ClassLoader
     *
     * @param parent parentClassLoader
     * @param className 自定义ClassLoader的类名
     * @return 包装了parentClassLoader的自定义ClassLoader
     */
    @Suppress("UNCHECKED_CAST")
    private fun wrapWithCustomClassLoader(parent: ClassLoader, className: String): ClassLoader {
        val classLoaderClass: Class<ClassLoader> = Class.forName(className, true, parent) as Class<ClassLoader>

        // 尝试使用各种类型的构造参数, 去对自定义的ClassLoader去进行实例化...

        // ClassLoader(parent)
        var classLoader = newClassLoader(classLoaderClass, PARENT_ONLY_PARAMS, arrayOf(parent))
        if (classLoader == null) {
            // ClassLoader(URLs, parent)
            classLoader = newClassLoader(classLoaderClass, URLS_AND_PARENT_PARAMS, arrayOf(NO_URLS, parent))
        }
        if (classLoader == null) {
            // ClassLoader()
            classLoader = newClassLoader(classLoaderClass, NO_PARAMS, emptyArray())
        }
        return classLoader ?: throw IllegalArgumentException("Unable to create class loader for $className")
    }

    /**
     * 尝试对给定的[loaderClass], 去进行实例化[ClassLoader]
     *
     * @param loaderClass 要去进行实例化的ClassLoader的类型
     * @param parameterTypes 构造器参数类型列表
     * @param args 构造器参数列表
     * @return 如果实例化成功, return 实例化得到的ClassLoader; 实例化失败return null
     */
    @Nullable
    private fun newClassLoader(
        loaderClass: Class<ClassLoader>,
        parameterTypes: Array<Class<*>>,
        args: Array<Any?>
    ): ClassLoader? {
        try {
            val constructor = loaderClass.getDeclaredConstructor(*parameterTypes)
            constructor.isAccessible = true
            return constructor.newInstance(*args) as ClassLoader
        } catch (ex: NoSuchMethodException) {
            return null
        }
    }

    @Nullable
    private fun getProperty(propertyKey: String): String? {
        return getProperty(propertyKey, null, null)
    }

    @Nullable
    private fun getProperty(propertyKey: String, manifestKey: String?): String? {
        return getProperty(propertyKey, manifestKey, null)
    }

    @Nullable
    private fun getPropertyWithDefault(propertyKey: String, @Nullable defaultValue: String?): String? {
        return getProperty(propertyKey, null, defaultValue)
    }

    private fun getProperty(
        propertyKey: String,
        @Nullable manifestKey: String?,
        @Nullable defaultValue: String?
    ): String? {

        var manifestKeyToUse = manifestKey
        if (manifestKeyToUse == null) {
            manifestKeyToUse = propertyKey.replace('.', '-')
            manifestKeyToUse = toCamelCase(manifestKeyToUse)
        }

        // 先尝试直接使用propertyKey从SystemProperties当中去进行getProperty
        val property = SystemPropertiesUtils.getProperty(propertyKey)
        if (property != null) {
            val value = SystemPropertiesUtils.resolvePlaceholders(properties, property)
            debug("Property '$propertyKey' from environment '$value'")
            return value
        }

        // 再次尝试, 从Properties当中去进行getProperty
        if (properties.containsKey(propertyKey)) {
            val value = SystemPropertiesUtils.resolvePlaceholders(properties, propertyKey)
            debug("Property '$propertyKey' from properties '$value'")
            return value
        }

        // 再次尝试, 从home Manifest当中去进行getProperty
        try {
            val explodedArchive = ExplodedArchive(this.home, false)
            try {
                val manifest = explodedArchive.getManifest()
                if (manifest != null) {
                    val value = manifest.mainAttributes.getValue(manifestKeyToUse)
                    if (value != null) {
                        debug("Property '$manifestKeyToUse' from home directory manifest '$value'")
                        return SystemPropertiesUtils.resolvePlaceholders(this.properties, value)
                    }
                }

            } finally {
                explodedArchive.close()
            }

        } catch (ex: Exception) {
            // ignore
        }

        // 再次尝试, 从archive的Manifest当中去进行获getProperty
        val manifest = createArchive().getManifest()
        if (manifest != null) {
            val value = manifest.mainAttributes.getValue(manifestKeyToUse)
            if (value != null) {
                debug("Property '$manifestKeyToUse' from archive manifest '$value'")
                return SystemPropertiesUtils.resolvePlaceholders(this.properties, value)
            }
        }

        // 尝试仍然没有结果, 那么返回默认值
        defaultValue ?: return null
        return SystemPropertiesUtils.resolvePlaceholders(this.properties, defaultValue)
    }

    @Nullable
    private fun toCamelCase(@Nullable string: String?): String? {
        string ?: return null
        val builder = StringBuilder()
        val matcher = WORD_SEPARATOR.matcher(string)
        var pos = 0
        while (matcher.find()) {
            builder.append(capitalize(string.substring(pos, matcher.end())))
            pos = matcher.end()
        }
        builder.append(capitalize(string.substring(pos, matcher.end())))
        return builder.toString()
    }

    private fun capitalize(str: String): String {
        return str[0].lowercase() + str.substring(1)
    }

    private fun initializeProperties() {
        val configs = ArrayList<String>()
        if (getProperty(CONFIG_LOCATION) != null) {
            configs += getProperty(CONFIG_LOCATION)!!
        } else {
            val names = getPropertyWithDefault(CONFIG_LOCATION, "loader")!!.split(",")
            for (name in names) {
                configs += "file:" + getHomeDirectory() + "/" + name + ".properties"
                configs += "classpath:$name.properties"
                configs += "classpath:BOOT-INF/classes/$name.properties"
            }
        }

        for (config in configs) {
            val resource = getResource(config)
            if (resource == null) {
                debug("Not found: $config")
            } else {
                debug("Found: $config")
                loadResource(resource)
                // 只要加载到一个, 默认就行了, return
                return
            }
        }
    }

    /**
     * 如果开启了debug, 那么需要输出相关的message信息
     *
     * @param message message
     */
    private fun debug(message: String) {
        if (System.getProperty(DEBUG) == "true") {
            println(message)
        }
    }

    @Nullable
    private fun getResource(config: String): InputStream? {
        if (config.startsWith("classpath:")) {
            return getClasspathResource(config.substring("classpath:".length))
        }

        return javaClass.getResourceAsStream(config)
    }


    private fun handleUrl(path: String): String {
        return path
    }

    /**
     * 获取文件资源
     *
     * @param config 配置文件路径
     * @return 加载到的配置文件的输入流(无法加载到return null)
     */
    @Nullable
    private fun getFileResource(config: String): InputStream? {
        val file = File(config)
        debug("Trying file: $config")
        return if (file.canRead()) FileInputStream(file) else null
    }

    /**
     * 获取URL资源
     *
     * @param config 配置文件位置url
     * @return 加载到的配置文件的输入流(无法加载到return null)
     */
    @Nullable
    private fun getURLResource(config: String): InputStream? {
        val url = URL(config)

        return null
    }

    /**
     * 获取Classpath的资源
     *
     * @param config 配置文件路径
     * @return 加载到的配置文件的输入流(无法加载到return null)
     */
    @Nullable
    private fun getClasspathResource(config: String): InputStream? {
        var configToUse = config
        while (configToUse.startsWith('/')) {
            configToUse = configToUse.substring(1)
        }
        // 拼接前缀"/"标识基于类路径去进行绝对定位
        configToUse = "/$configToUse"
        debug("Trying classpath: $configToUse")
        return javaClass.getResourceAsStream(configToUse)
    }


    private fun loadResource(resource: InputStream) {
        this.properties.load(resource)
    }
}