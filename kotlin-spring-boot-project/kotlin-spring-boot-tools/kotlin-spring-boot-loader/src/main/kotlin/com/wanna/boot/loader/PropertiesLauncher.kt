package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive
import com.wanna.boot.loader.archive.ExplodedArchive
import com.wanna.boot.loader.archive.JarFileArchive
import com.wanna.boot.loader.util.SystemPropertiesUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern
import javax.annotation.Nullable

/**
 * 基于Properties裂行配置文件的方式去对应用去进行启动的[Launcher], 用户可以在Properties文件当中去指定
 * 存放Archive归档文件(JAR/WAR)的classpath以及应用程序的主启动类mainClass
 *
 * 使用这种方式去进行启动应用, 相比于基于一个可执行的JAR包的方式, 可以变得更加灵活和可控.
 *
 * 当前的[Launcher]支持在一个".properties"配置文件当中去配置Loader的相关配置信息, 默认是在当前的classpath下
 * 和当前的工作目录下去进行寻找"loader.properties"配置文件去进行启动. 如果想要去进行自定义配置文件的文件名,
 * 那么也可以使用"loader.config.name"系统属性去进行自定义, 例如通过下面这样的VM参数"-Dloader.config.name=xxx",
 * 即可让配置文件变成xxx, 这样当前的[Launcher]就可以去寻找"xxx.properties"去作为启动的相关配置信息.
 *
 * 通过"loader.path"属性, 可以配置一些使用","去进行分隔的一系列目录, 这些目录下的文件资源或者是"*.jar"/".zip"当中的嵌套的Archive,
 * 也都会被添加到classpath当中去, 对于"BOOT-INF/classes/"和"BOOT-INF/lib/"目录, 通常情况下, 都是会启用的.
 *
 * 通过"loader.main"属性, 用于去配置一个用于程序的启动的主启动类(必须包含有main方法), 如果没有配置"loader.main"属性的话,
 * 那么将会从尝试"MANIFEST.MF"文件当中去提取"Start-Class"属性去作为主启动类.
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
         * 当前是否是Debug程序, 在Debug的情况下, 可以输出引导应用启动过程当中, 输出的更多的日志信息
         */
        private const val LOADER_DEBUG = "loader.debug"

        /**
         * 主启动类的属性Key, 也可以通过Manifest的"Start-Class"去进行指定
         */
        const val LOADER_MAIN = "loader.main"

        /**
         * Loader要使用的自定义的ClassLoader, 不使用自定义ClassLoader的话, 直接使用Launcher所创建的[LaunchedURLClassLoader]即可
         */
        private const val LOADER_CLASSLOADER = "loader.classLoader"

        /**
         * Loader要额外使用的命令行参数列表(多个参数之间使用','去进行分隔), 将会被merge到命令行参数当中去进行启动
         */
        private const val LOADER_ARGS = "loader.args"

        /**
         * Loader的Home目录(默认使用"user.dir"去作为Home目录)
         */
        private const val LOADER_HOME = "loader.home"

        /**
         * LoaderPath, 对于启动应用需要使用的依赖的路径, 对于这些路径下的类/Jar包将会作为应用程序的Classpath;
         * 可以配置绝对路径, 比如"/tmp/lib", 也可以配置相对路径, 例如"lib"(会以"loader.home"去作为基准地址去进行寻找)
         */
        private const val LOADER_PATH = "loader.path"

        /**
         * Loader的配置文件路径
         */
        private const val CONFIG_LOCATION = "loader.config.location"

        /**
         * Loader的配置文件属性名(默认为loader, 也就是会对应"loader.properties"配置文件)
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

        /**
         * 嵌套Archive的分隔符
         */
        @JvmStatic
        private val NESTED_ARCHIVE_SEPARATOR = "!" + File.separator


        /**
         * 基于[PropertiesLauncher]去引导应用程序的启动的主入口main方法
         *
         * @param args 命令行参数
         * @throws Exception 如果启动应用程序失败
         */
        @Throws(Exception::class)
        @JvmStatic
        fun main(vararg args: String) {
            // 创建PropertiesLauncher, 完成配置文件的加载
            val propertiesLauncher = PropertiesLauncher()

            // 将loader.args属性当中的参数, 去merge到原始的命令行参数之前
            val argsToUse = propertiesLauncher.getArgs(arrayOf(*args))

            // 使用Launcher.launch去引导应用程序的启动
            propertiesLauncher.launch(argsToUse)
        }
    }

    /**
     * Loader的工作目录(HomeDirectory), 可以通过"loader.home"去进行指定, 默认为"user.dir"用户家目录
     */
    private val home: File

    /**
     * 存放相关配置信息的Properties(默认是从"loader.properties"去加载得到)
     */
    private val properties = Properties()

    /**
     * paths, 维护了需要去进行加载的类的路径列表, 其实就是用户自定义的classpath, 可以通过"loader.path"去进行配置
     */
    private var paths: List<String> = ArrayList<String>()

    /**
     * parent Archive
     */
    private val parent: Archive

    /**
     * ClassPath下的[Archive]归档文件列表, 对于这些[Archive], 将会被[LaunchedURLClassLoader]去进行类的加载
     */
    @Nullable
    private var classPathArchives: ClassPathArchives? = null

    init {
        try {
            // 1.初始化Loader的HomeDirectory
            this.home = this.getHomeDirectory()

            // 2.加载Loader的.properties配置文件
            initializeProperties()

            // 3.初始化Paths, 找到用户需要去进行加载的类的来源的目录
            initializePaths()

            // 4.创建parent Archive
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
        return getProperty(LOADER_MAIN, MANIFEST_KEY_START_CLASS)
            ?: throw IllegalStateException("No '$LOADER_MAIN' or '$MANIFEST_KEY_START_CLASS' specified")
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

        // 使用自定义的ClassLoader去包装LaunchedURLClassLoader
        val customClassLoader = wrapWithCustomClassLoader(loader, classLoaderName)
        debug("Using custom class loader: $classLoaderName")
        return customClassLoader
    }

    /**
     * 获取ClassPath下的Archive列表的迭代器
     *
     * @return Iterator of Archive
     */
    override fun getClassPathArchivesIterator(): Iterator<Archive> {
        var classPathArchives = this.classPathArchives
        if (classPathArchives == null) {
            classPathArchives = ClassPathArchives()
            this.classPathArchives = classPathArchives
        }
        return classPathArchives.iterator()
    }


    /**
     * 获取到Loader的Home工作目录
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

    /**
     * 根据原始的命令行参数, 去添加"loader.args"当中去进行配置的参数去进行merge
     *
     * Note: "loader.args"参数会在前面, 原始的命令行参数会在后面, 保证命令行给定的参数可以有更高优先级
     *
     * @param args 原始的命令行参数
     * @return merge完成"loader.args"的配置当中的参数列表之后得到的最终命令行参数
     */
    protected open fun getArgs(args: Array<String>): Array<String> {
        // 解析loader.args参数
        val loaderArgs = getProperty(LOADER_ARGS) ?: return args
        val defaultArgs = loaderArgs.split("\\s+")
        return (defaultArgs + args).toTypedArray()
    }

    /**
     * 初始化Loader要去进行加载的类的资源路径
     */
    private fun initializePaths() {
        val path = getProperty(LOADER_PATH)
        if (path != null) {
            this.paths = parsePathsProperty(path)
        }
        debug("Nested archive paths: $paths")
    }

    /**
     * 将用户给定的使用","去进行分割的自定义的路径列表, 去解析成为列表
     *
     * @param commaSeparatedPaths 使用","去分隔的多个路径
     * @return 使用","去拆分得到的路径列表
     */
    private fun parsePathsProperty(commaSeparatedPaths: String): List<String> {
        val paths = ArrayList<String>()

        // 对于用户给定的所有的路径去进行拆分
        for (path in commaSeparatedPaths.split(",")) {
            var pathToUse = cleanupPath(path)

            // 如果path=""的话, 说明需要使用root目录("/")
            pathToUse = pathToUse.ifBlank { "/" }
            paths += pathToUse
        }

        // 如果没有结果的话, 那么将lib目录去作为要去进行加载的路径
        if (paths.isEmpty()) {
            paths += "lib"
        }
        return paths
    }

    /**
     * 将路径去进行清理干净
     *
     * @param path 待处理的path
     * @return clean path
     */
    private fun cleanupPath(path: String): String {
        var pathToUse = path.trim()
        if (pathToUse.startsWith("./")) {
            pathToUse = pathToUse.substring(2)
        }
        val lowercasePath = path.lowercase(Locale.ENGLISH)
        if (lowercasePath.endsWith(".jar") || lowercasePath.endsWith(".zip")) {
            return pathToUse
        }
        if (pathToUse.endsWith("/*")) {
            return pathToUse.substring(2)
        } else {
            if (!pathToUse.endsWith("/") && pathToUse != ".") {
                pathToUse = "$pathToUse/"
            }
        }
        return pathToUse
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
            val property = properties.getProperty(propertyKey)
            val value = SystemPropertiesUtils.resolvePlaceholders(properties, property)
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
        builder.append(capitalize(string.substring(pos, string.length)))
        return builder.toString()
    }

    private fun capitalize(str: String): String {
        return str[0].lowercase() + str.substring(1)
    }

    /**
     * 初始化Properties, 去执行配置文件的加载, 并保持到[properties]字段当中去
     *
     * @see CONFIG_NAME
     * @see CONFIG_LOCATION
     */
    private fun initializeProperties() {
        // 计算得到所有的要去进行加载的配置文件的路径
        val configs = ArrayList<String>()
        if (getProperty(CONFIG_LOCATION) != null) {
            configs += getProperty(CONFIG_LOCATION)!!
        } else {
            val names = getPropertyWithDefault(CONFIG_NAME, "loader")!!.split(",")
            for (name in names) {
                configs += "file:" + getHomeDirectory() + "/" + name + ".properties"
                configs += "classpath:$name.properties"
                configs += "classpath:BOOT-INF/classes/$name.properties"
            }
        }

        // 对计算得到的所有的配置文件的路径去执行资源的加载
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
     * 对给定的配置文件去执行资源的加载
     *
     * @param config 配置文件路径
     * @return 加载得到的资源输入流(加载不到return null)
     */
    @Nullable
    private fun getResource(config: String): InputStream? {
        var configToUse = config
        if (configToUse.startsWith("classpath:")) {
            return getClasspathResource(configToUse.substring("classpath:".length))
        }

        configToUse = handleUrl(configToUse)
        if (isUrl(configToUse)) {
            return getURLResource(configToUse)
        }

        return getFileResource(configToUse)
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

    /**
     * 获取URL资源
     *
     * @param config 配置文件位置url
     * @return 加载到的配置文件的输入流(无法加载到return null)
     */
    @Nullable
    private fun getURLResource(config: String): InputStream? {
        val url = URL(config)

        // 如果该URL存在的话, 那么通过Connection去获取到InputStream
        if (exists(url)) {
            val connection = url.openConnection()
            try {
                return connection.getInputStream()
            } catch (ex: IOException) {
                if (connection is HttpURLConnection) {
                    connection.disconnect()
                }
                throw ex
            }
        }
        return null
    }

    private fun isUrl(path: String): Boolean {
        return path.contains("://")
    }

    /**
     * 对URL去进行处理
     *
     * @param path path
     * @return 处理之后得到的Url
     */
    private fun handleUrl(path: String): String {
        var pathToUse = path
        if (pathToUse.startsWith("jar:file:") || pathToUse.startsWith("file:")) {
            pathToUse = URLDecoder.decode(pathToUse, StandardCharsets.UTF_8)
            if (pathToUse.startsWith("file:")) {
                pathToUse = pathToUse.substring("file:".length)
                if (pathToUse.startsWith("//")) {
                    pathToUse = pathToUse.substring(2)
                }
            }
        }
        return pathToUse
    }

    /**
     * 检查给定的URL是否存在?
     *
     * @param url URL
     * @return 如果该URL存在, return true; 否则return false
     */
    private fun exists(url: URL): Boolean {
        val connection = url.openConnection()
        try {
            connection.useCaches = connection.javaClass.simpleName.startsWith("JNLP")

            // 如果是HttpURLConnection的话, 那么探查responseCode去进行检查
            if (connection is HttpURLConnection) {
                connection.requestMethod = "HEAD"
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return true
                }
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    return false
                }
            }
            // 如果不是HttpURLConnection的话, 那么检查contentLength
            return connection.contentLength >= 0
        } finally {
            if (connection is HttpURLConnection) {
                connection.disconnect()
            }
        }
    }


    private fun loadResource(resource: InputStream) {
        this.properties.load(resource)

        // TODO resolve Placeholder
    }

    /**
     * 如果通过"loader.debug"属性去开启了debug, 那么需要输出相关的message信息
     *
     * @param message message
     */
    private fun debug(message: String) {
        // Note: 这里只能使用System.getProperty, 不然会导致无限递归导致SOF
        if (System.getProperty(LOADER_DEBUG) == "true") {
            // Note: 这里没有Logger, 也无法使用Logger, 只能使用System.out输出一下...
            println(message)
        }
    }


    /**
     * ClassPath下的Archive归档文件列表
     */
    private inner class ClassPathArchives : Iterable<Archive> {

        /**
         * 收集出来所有应该被作为ClassPath下的[Archive]归档文件
         */
        private val classpathArchives = ArrayList<Archive>()

        /**
         * 统计得到所有[Archive]当中, 对于[JarFileArchive]类型的列表, 统一管理, 方便统一close
         */
        private val jarFileArchives = ArrayList<JarFileArchive>()

        init {
            // 根据所有的"loader.path"去进行配置的路径, 去进行Archive的解析
            for (path in this@PropertiesLauncher.paths) {
                for (archive in getClassPathArchives(path)) {
                    addClassPathArchive(archive)
                }
            }

            // 添加内部嵌套的Archive
            addNestedEntries()
        }

        /**
         * 根据给定的path, 去解析得到[Archive]列表
         *
         * @param path path
         * @return 该路径下的解析得到的Archive列表
         */
        private fun getClassPathArchives(path: String): List<Archive> {
            val root = cleanupPath(handleUrl(path))
            val lib = ArrayList<Archive>()

            var file = File(root)
            if (root != "/") {
                // 如果root不是一个绝对路径的话, 那么使用home作为相对路径的基地址, 去进行路径的计算
                if (!isAbsolutePath(root)) {
                    file = File(this@PropertiesLauncher.home, root)
                }

                // 如果给的是一个文件夹的话, 那么创建一个ExplodedArchive(解压缩的之后的包)
                if (file.isDirectory) {
                    debug("Adding classpath entries from $file")
                    val archive = ExplodedArchive(file, false)
                    lib += archive
                }
            }

            // 如果给定的路径, 是一个非嵌套的Jar包的话, 那么执行解析并收集起来...
            val archive = getArchive(file)
            if (archive != null) {
                debug("Adding classpath entries from archive ${archive.getUrl()}$root")
                lib += archive
            }

            // 如果给定的路径, 是一个嵌套的Jar包的话, 那么执行解析并收集起来
            val nestedArchives = getNestedArchives(root)
            if (!nestedArchives.isNullOrEmpty()) {
                debug("Adding classpath entries from nested $root")
                lib += nestedArchives
            }
            return lib
        }

        /**
         * 判断给定的路径, 是否是一个绝对路径?
         *
         * @param root path
         * @return 如果是绝对路径, return true; 否则return false
         */
        private fun isAbsolutePath(root: String): Boolean {
            // 对于Windows操作系统含有":", 例如"C:"表示C盘下的目录, 对于Macos/Linux等系统, 应该以"/"开头
            return root.contains(":") || root.startsWith("/")
        }

        /**
         * 如果给定的[File]是文件类型是".jar"/".zip", 那么返回该文件对应的[JarFileArchive]
         *
         * @param file File
         * @return 如果是Jar包的话, 那么返回JarFileArchive, 否则return null
         */
        @Nullable
        private fun getArchive(file: File): Archive? {
            // 如果是嵌套的Archive, 那么跳过, 在这里暂时不处理
            if (isNestedArchivePath(file)) {
                return null
            }
            val name = file.name.lowercase(Locale.ENGLISH)
            if (name.endsWith(".zip") || name.endsWith(".jar")) {
                return getJarFileArchive(file)
            }
            return null
        }

        /**
         * 根据给定的path, 去获取到嵌套的[Archive]
         *
         * @param path path
         * @return 根据path去解析得到的嵌套的Archive列表(or null)
         */
        @Nullable
        private fun getNestedArchives(path: String): List<Archive>? {
            return null
        }

        /**
         * 根据给定的[File]去构建[JarFileArchive], 并将该[JarFileArchive]去缓存起来统一管理
         *
         * @param file File
         * @return JarFileArchive
         */
        private fun getJarFileArchive(file: File): JarFileArchive {
            val jarFileArchive = JarFileArchive(file)
            this.jarFileArchives += jarFileArchive
            return jarFileArchive
        }

        /**
         * 判断给定的[File]路径是否是一个内部嵌套的Archive
         *
         * @param file file
         * @return 如果路径当中含有"!/", 那么就说明是嵌套的Archive
         */
        private fun isNestedArchivePath(file: File): Boolean {
            return file.path.contains(NESTED_ARCHIVE_SEPARATOR)
        }

        private fun addClassPathArchive(archive: Archive) {
            if (archive !is ExplodedArchive) {
                this.classpathArchives += archive
                return
            }
            this.classpathArchives += archive
        }

        /**
         * 添加parent对应的Archive内部嵌套的Archive, 因为当前[PropertiesLauncher]所在的Jar,
         * 一般是会被解压去放入到用户的应用程序的最终FatJar当中的, 在FatJar当中, 就往往
         * 会有"BOOT-INF/classes/"和"BOOT-INF/lib/"目录, 需要把这些去添加到ClassPath下
         */
        private fun addNestedEntries() {
            // parent Archive当中可能存在有"BOOT-INF/classes/"或者是"BOOT-INF/lib/"这样的目录
            // 意味着我们正运行在一个可执行的JAR包当中, 我们也应该把这些嵌套的Archive在最后去进行收集起来
            try {
                val archives = this@PropertiesLauncher.parent.getNestedArchives(
                    { true },
                    JarLauncher.NESTED_ARCHIVE_ENTRY_FILTER
                )
                for (archive in archives) {
                    this.classpathArchives += archive
                }
            } catch (ex: IOException) {
                // ignore
            }
        }

        override fun iterator(): Iterator<Archive> {
            return this.classpathArchives.iterator()
        }

        @Throws(IOException::class)
        fun close() {
            for (jarFileArchive in this.jarFileArchives) {
                jarFileArchive.close()
            }
        }
    }

}