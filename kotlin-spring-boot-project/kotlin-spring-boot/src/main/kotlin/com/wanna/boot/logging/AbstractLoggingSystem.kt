package com.wanna.boot.logging

import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.io.ClassPathResource
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils

/**
 * 为[LoggingSystem]提供了抽象的实现, 提供了对于为日志组件去提供日志文件的加载的相关的功能支持;
 *
 * * 1.如果自己指定了配置文件的路径, 将会按照"logging.config"配置的配置文件路径去进行加载配置文件
 * * 2.如果没有指定配置文件的路径的话, 那么将会按照如下的顺序尝试去进行加载:
 *      * 2.1 按照日志组件默认的配置文件去进行加载, 例如"logback.xml";
 *      * 2.2 按照Spring自定义的配置文件去进行加载, 例如"logback-spring.xml";
 *      * 2.3 按照SpringBoot自定义的默认的配置信息去进行加载.
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @param classLoader ClassLoader to use
 */
abstract class AbstractLoggingSystem(val classLoader: ClassLoader) : LoggingSystem() {

    companion object {

        /**
         * [LoggerConfiguration]的比较器
         */
        @JvmStatic
        val CONFIGURATION_COMPARATOR: Comparator<LoggerConfiguration> = LoggerConfigurationComparator(ROOT_LOGGER_NAME)
    }


    /**
     * 执行对于[LoggingSystem]的初始化, 主要是去进行日志组件相关的配置文件的加载
     *
     * @param context context
     * @param configLocation 配置文件的所在位置
     * @param logFile LogFile
     */
    override fun initialize(
        context: LoggingInitializationContext,
        @Nullable configLocation: String?,
        @Nullable logFile: LogFile?
    ) {
        // 如果手动指定了配置文件的位置的话, 那么使用指定的配置文件
        if (StringUtils.hasLength(configLocation)) {
            initializeWithSpecificConfig(context, configLocation!!, logFile)
            return
        }

        // 如果没有手动指定配置文件的位置的话, 那么使用默认的配置文件
        initializeWithConventions(context, logFile)
    }

    /**
     * 如果明确给定了日志文件的话, 那么使用给定的配置文件去完成初始化
     *
     * @param context context
     * @param configLocation 配置文件的路径
     * @param logFile LogFile
     */
    private fun initializeWithSpecificConfig(
        context: LoggingInitializationContext,
        configLocation: String,
        @Nullable logFile: LogFile?
    ) {
        loadConfiguration(context, configLocation, logFile)
    }

    /**
     * 如果没有明确给定日志文件的话, 那么应该使用约定的配置文件, 去完成初始化
     *
     * @param context context
     * @param logFile logFile
     */
    private fun initializeWithConventions(context: LoggingInitializationContext, @Nullable logFile: LogFile?) {
        // 1.先尝试去加载日志组件自身的默认配置文件
        var config = getSelfInitializationConfig()
        if (config != null && logFile == null) {
            // 如果这里可以加载到配置问价, 说明日志组件在之前就自我初始化已经完成了,
            // 那么需要尝试去进行重新初始化, 因为有可能属性发生了变化
            reinitialize(context)
            return
        }

        // 2. 如果没有加载到默认的日志组件的配置文件, 那么尝试去加载Spring的配置文件
        if (config == null) {
            config = getSpringInitializationConfig()
        }

        // 3.如果加载到了Spring的配置文件, 那么需要去进行配置文件的加载
        if (config != null) {
            loadConfiguration(context, config, logFile)
            return
        }

        // 4.如果仍然没有加载到配置文件, 那么尝试去加载SpringBoot去定义的默认的配置信息...
        loadDefaults(context, logFile)
    }

    /**
     * 加载配置文件
     *
     * @param context context
     * @param configLocation 配置文件的路径
     * @param logFile logFile
     */
    protected open fun loadConfiguration(
        context: LoggingInitializationContext,
        configLocation: String,
        @Nullable logFile: LogFile?
    ) {

    }

    /**
     * 如果没有找到日志组件的配置文件的话, 那么尝试去加载SpringBoot去定义的默认的配置信息
     *
     * @param context context
     * @param logFile LogFile
     */
    protected open fun loadDefaults(context: LoggingInitializationContext, @Nullable logFile: LogFile?) {

    }

    /**
     * 如果在初始化之前就已经完成了日志组件的初始化了, 那么需要尝试去进行重新初始化, 因为很可能很多配置信息已经发生了变更
     *
     * @param context context
     */
    protected open fun reinitialize(context: LoggingInitializationContext) {

    }

    /**
     * 获取日志组件本身的用于去进行初始化的配置文件的路径
     *
     * @return 日志组件的默认配置文件当中, 用于去初始化日志组件本身的配置文件路径(如果用户没有指定配置文件的话, return null)
     */
    @Nullable
    protected open fun getSelfInitializationConfig(): String? {
        return findConfig(getStandardConfigLocations())
    }

    /**
     * 获取Spring针对当前日志组件, 去进行初始化的配置文件
     *
     * @return Spring支持的配置文件当中, 用于去初始化日志组件本身的配置文件路径(如果用户没有指定配置文件的话, return null)
     */
    @Nullable
    protected open fun getSpringInitializationConfig(): String? {
        return findConfig(getSpringConfigLocations())
    }

    /**
     * 从给定的这些候选路径当中, 去找到一个在classpath下确实存在的配置文件, 如果都不存在return null
     *
     * @param locations 候选的配置文件的路径列表
     * @return 真实存在的用户手动去进行给定的配置文件路径
     */
    @Nullable
    private fun findConfig(locations: Array<String>): String? {
        for (location in locations) {
            if (ClassPathResource(location, this.classLoader).exists()) {
                return "classpath:$location"
            }
        }
        return null
    }

    /**
     * 获取当前[LoggingSystem]对应的日志组件, 支持去进行处理的标准的配置文件的位置
     *
     * @return 日志组件默认的标准的配置文件的位置
     */
    protected abstract fun getStandardConfigLocations(): Array<String>

    /**
     * 获取该日志组件对应的Spring的配置文件路径裂帛啊呸(在文件扩展名之前添加"-spring", 比如"logback.xml"->"logback-spring.xml")
     *
     * @return 解析得到该日志组件对应的Spring的配置文件路径列表
     */
    protected open fun getSpringConfigLocations(): Array<String> {
        val locations = getStandardConfigLocations()
        for (index in locations.indices) {
            val extension = StringUtils.getFilenameExtension(locations[index])
                ?: throw IllegalStateException("file extension name not exists, path=${locations[index]}")
            locations[index] =
                locations[index].substring(0, locations[index].length - extension.length - 1) + "-spring." + extension
        }
        return locations
    }

    /**
     * 获取基于子类所在包下的配置文件
     *
     * @param fileName fileName
     * @return 子类所在包下的指定文件名的配置文件路径
     */
    protected fun getPackagedConfigFile(fileName: String): String {
        var defaultPath = ClassUtils.getPackageName(javaClass)
        defaultPath = defaultPath.replace('.', '/')
        defaultPath = "$defaultPath/$fileName"
        defaultPath = "classpath:$defaultPath"
        return defaultPath
    }

    /**
     * 将[LoggingSystem]的Properties配置信息和[LogFile]对于日志文件的配置信息, 应用到SystemProperties当中
     *
     * @param environment Environment
     * @param logFile LogFile(日志文件的路径/文件名)
     */
    protected fun applySystemProperties(environment: ConfigurableEnvironment, @Nullable logFile: LogFile?) {
        LoggingSystemProperties(environment).apply(logFile)
    }

    /**
     * 维护系统的[LogLevel]与日志组件本地的LogLevel之间的映射关系
     *
     * @param T 日志组件本地的LogLevel类型
     */
    protected class LogLevels<T> {

        private val systemToNative = LinkedHashMap<LogLevel, T>()

        private val nativeToSystem = LinkedHashMap<T, LogLevel>()

        fun map(systemLevel: LogLevel, nativeLevel: T) {
            systemToNative[systemLevel] = nativeLevel
            nativeToSystem[nativeLevel] = systemLevel
        }

        @Nullable
        fun convertNativeToSystem(nativeLevel: T?): LogLevel? {
            nativeLevel ?: return null
            return nativeToSystem[nativeLevel]
        }

        @Nullable
        fun convertSystemToNative(systemLevel: LogLevel?): T? {
            systemLevel ?: return null
            return systemToNative[systemLevel]
        }

        fun getSupported(): Set<LogLevel> = systemToNative.keys.toSet()
    }

}