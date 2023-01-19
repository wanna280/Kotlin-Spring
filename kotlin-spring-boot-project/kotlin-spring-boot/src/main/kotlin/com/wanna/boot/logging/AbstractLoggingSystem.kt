package com.wanna.boot.logging

import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.io.ClassPathResource
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
abstract class AbstractLoggingSystem(val classLoader: ClassLoader) : LoggingSystem() {


    override fun initialize(
        context: LoggingInitializationContext,
        @Nullable configLocation: String?,
        logFile: LogFile
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
     * 使用特定的配置文件去完成初始化
     *
     * @param context context
     * @param configLocation 配置文件的路径
     * @param logFile LogFile
     */
    private fun initializeWithSpecificConfig(
        context: LoggingInitializationContext,
        configLocation: String,
        logFile: LogFile
    ) {
        loadConfiguration(context, configLocation, logFile)
    }

    /**
     * 使用约定的配置文件, 去完成初始化
     *
     * @param context context
     * @param logFile logFile
     */
    private fun initializeWithConventions(context: LoggingInitializationContext, @Nullable logFile: LogFile?) {
        var config = getSelfInitializationConfig()
        if (config != null && logFile == null) {
            // 如果之前, 自我初始化已经完成了, 那么尝试去进行重新初始化, 因为有可能属性发生了变化
            reinitialize(context)
            return
        }
        if (config == null) {
            config = getSpringInitializationConfig()
        }
        if (config != null) {
            loadConfiguration(context, config, logFile)
            return
        }
        loadDefaults(context, logFile)
    }

    protected open fun loadConfiguration(
        context: LoggingInitializationContext,
        configLocation: String,
        @Nullable logFile: LogFile?
    ) {

    }

    protected open fun loadDefaults(context: LoggingInitializationContext, @Nullable logFile: LogFile?) {

    }

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
            if (ClassPathResource("classpath:$location", this.classLoader).exists()) {
                return location
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
                locations[index].substring(0, locations[0].length - extension.length - 1) + "-spring." + extension
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
     * 将[LoggingSystem]的Properties配置信息, 去应用给给定的[LogFile]当中
     *
     * @param environment Environment
     * @param logFile LogFile
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

        fun convertNativeToSystem(nativeLevel: T): LogLevel {
            return nativeToSystem[nativeLevel] ?: throw IllegalStateException("no such native level")
        }

        fun convertSystemToNative(systemLevel: LogLevel): T {
            return systemToNative[systemLevel] ?: throw IllegalStateException("no such system level")
        }

        fun getSupported(): Set<LogLevel> = systemToNative.keys.toSet()
    }

}