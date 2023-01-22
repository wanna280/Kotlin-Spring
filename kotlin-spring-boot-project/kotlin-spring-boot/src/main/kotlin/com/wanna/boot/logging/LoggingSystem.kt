package com.wanna.boot.logging

import com.wanna.boot.logging.java.JavaLoggingSystem
import com.wanna.boot.logging.logback.LogbackLoggingSystem
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.util.StringUtils
import java.util.*

/**
 * 日志系统的抽象实现, 具体的日志系统通过继承这个类去实现自定义的功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @see JavaLoggingSystem
 * @see LogbackLoggingSystem
 */
abstract class LoggingSystem {
    /**
     * 获取到需要去应用给当前的[LoggingSystem]的Properties配置信息
     *
     * @param environment Environment
     * @return LoggingSystem Properties
     */
    open fun getSystemProperties(environment: ConfigurableEnvironment): LoggingSystemProperties {
        return LoggingSystemProperties(environment)
    }

    /**
     * 获取到应用程序关闭的ShutdownHandler
     *
     * @return ShutdownHandler Runnable
     */
    open fun getShutdownHandler(): Runnable? = null

    /**
     * 清理当前[LoggingSystem]
     */
    open fun cleanUp() {}

    /**
     * 在初始化之前, 需要去做的相关工作, 会在[initialize]方法执行之前被回调
     */
    open fun beforeInitialize() {}

    /**
     * 完成对于[LoggingSystem]的初始化工作
     *
     * @param context [LoggingSystem]初始化用到的上下文信息
     * @param configLocation 配置文件路径
     * @param logFile LogFile
     */
    open fun initialize(
        context: LoggingInitializationContext,
        @Nullable configLocation: String?,
        @Nullable logFile: LogFile?
    ) {
    }

    /**
     * 获取当前的[LoggingSystem]所支持的[LogLevel]
     *
     * @return supported LogLevel
     */
    open fun getSupportedLogLevels(): Set<LogLevel> = EnumSet.allOf(LogLevel::class.java)

    /**
     * 设置给定的loggerName的Logger的日志级别
     *
     * @param loggerName 要去修改日志级别的Logger
     * @param logLevel 要去使用的日志级别
     */
    open fun setLogLevel(loggerName: String, @Nullable logLevel: LogLevel?) {
        throw UnsupportedOperationException("Unable to set log level")
    }

    /**
     * 获取当前[LoggingSystem]当中的所有的Logger的配置信息
     *
     * @return Logger的配置信息
     */
    open fun getLoggerConfigurations(): List<LoggerConfiguration> {
        throw UnsupportedOperationException("Unable to get logger configurations")
    }

    /**
     * 获取到当前[LoggingSystem]当中给定的Logger的配置信息
     *
     * @param loggerName LoggerName
     * @return 该Logger的配置信息(不存在的话, return null)
     */
    @Nullable
    open fun getLoggerConfiguration(loggerName: String): LoggerConfiguration? {
        throw UnsupportedOperationException("Unable to get logger configuration")
    }

    companion object {

        /**
         * 通过这个系统属性, 可以去自定义特殊的[LoggingSystem]的实现, 否则将会根据当前系统的依赖情况去进行自动推断
         *
         * @see System.getProperty
         */
        @JvmField
        val SYSTEM_PROPERTY: String = LoggingSystem::class.java.name

        /**
         * Root Logger Name
         */
        const val ROOT_LOGGER_NAME = "ROOT"

        /**
         * 使用NoOp的LoggingSystem实现
         */
        const val NONE = "none"

        /**
         * 默认的[LoggingSystemFactory]实现, 自动推断出合适的[LoggingSystem]去进行使用
         */
        @JvmStatic
        private val SYSTEM_FACTORY = LoggingSystemFactory.fromSpringFactories()

        /**
         * 根据用户是否进行自定义[LoggingSystem], 选择出来一个合适的[LoggingSystem]
         *
         * @param classLoader ClassLoader to use
         * @return LoggingSystem
         */
        @JvmStatic
        fun get(classLoader: ClassLoader): LoggingSystem {
            val systemLoggingSystem = System.getProperty(SYSTEM_PROPERTY)

            // 如果用户有自定义, 那么检查是否是"none"?
            if (StringUtils.hasLength(systemLoggingSystem)) {
                if (NONE == systemLoggingSystem) {
                    return NoOpLoggingSystem()
                }
                return get(classLoader, systemLoggingSystem)
            }

            // 如果用户没有去进行自定义LoggingSystem, 那么根据当前应用的依赖情况, 去进行自动推断
            val loggingSystem = SYSTEM_FACTORY.getLoggingSystem(classLoader)
            return loggingSystem ?: throw IllegalStateException("No suitable logging system located")
        }

        /**
         * 为指定的[LoggingSystem]的类去进行实例化
         *
         * @param classLoader ClassLoader
         * @param systemLoggingSystem 自定义的LoggingSystem的类
         * @return 实例化得到的[LoggingSystem]
         */
        @JvmStatic
        fun get(classLoader: ClassLoader, systemLoggingSystem: String): LoggingSystem {
            try {
                val loggingSystemClass = ClassUtils.forName<LoggingSystem>(systemLoggingSystem)
                val loggingSystemConstructor = loggingSystemClass.getDeclaredConstructor(ClassLoader::class.java)
                ReflectionUtils.makeAccessible(loggingSystemConstructor)
                return BeanUtils.instantiateClass(loggingSystemConstructor, classLoader)
            } catch (ex: Exception) {
                throw IllegalStateException(ex)
            }
        }
    }


    /**
     * 不进行任何操作的[LoggingSystem]
     */
    private class NoOpLoggingSystem : LoggingSystem() {
        override fun beforeInitialize() {}
        override fun setLogLevel(loggerName: String, @Nullable logLevel: LogLevel?) {}
        override fun getLoggerConfigurations(): List<LoggerConfiguration> = emptyList()

        @Nullable
        override fun getLoggerConfiguration(loggerName: String): LoggerConfiguration? = null
    }
}