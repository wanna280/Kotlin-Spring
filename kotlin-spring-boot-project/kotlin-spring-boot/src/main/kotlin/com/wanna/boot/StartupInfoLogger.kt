package com.wanna.boot

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.StopWatch
import org.slf4j.Logger
import java.lang.management.ManagementFactory

/**
 * 以日志的形式去进行记录Spring应用启动过程当中的相关信息, 比如相关的JavaVersion, Spring应用启动所花费的时间;
 *
 * @param sourceClass 源类(主启动类), 如果不设置默认为"application"
 */
class StartupInfoLogger(@Nullable private val sourceClass: Class<*>?) {

    /**
     * 记录SpringApplication启动过程当中的相关的信息
     *
     * @param logger 日志组件
     */
    fun logStarting(logger: Logger) {
        if (logger.isInfoEnabled) {
            logger.info(getStartingMessage())
        }
    }


    /**
     * 记录SpringApplication已经完成启动的日志
     *
     * @param logger 日志组件
     * @param stopWatch 启动SpringApplication所用的的秒表
     */
    fun logStarted(logger: Logger, stopWatch: StopWatch) {
        if (logger.isInfoEnabled) {
            logger.info(getStartedMessage(stopWatch))
        }
    }

    /**
     * 获取Spring应用开始启动的相关消息, 包括JavaVersion、PID、ImplementationVersion等信息
     */
    private fun getStartingMessage(): String {
        val builder = StringBuilder()
        builder.append("启动SpringApplication[")
        builder.append(getApplicationName())
        builder.append("]")
        builder.append(", 使用的JavaVersion为[")
        builder.append(System.getProperty("java.version"))
        builder.append("], 进程PID为[")
        try {
            val pid = ManagementFactory.getRuntimeMXBean().pid
            builder.append(pid)
        } catch (ignored: Exception) {

        }
        builder.append("], ProjectVersion=[")
        builder.append(sourceClass?.`package`?.implementationVersion ?: "")
        builder.append("], userName=[")
        builder.append(System.getProperty("user.name"))
        builder.append("], userDir=[")
        builder.append(System.getProperty("user.dir"))
        builder.append("]")
        return builder.toString()
    }

    /**
     * 获取Spring应用已经启动的相关信息, 主要包括SpringApplication启动的时间, 以及JVM的运行时间
     */
    private fun getStartedMessage(stopWatch: StopWatch): String {
        val builder = StringBuilder("SpringApplication[")
        builder.append(getApplicationName())
        builder.append("]启动成功, 启动所花费的时间为[")
        builder.append(stopWatch.getTotalTimeMills() / 1000.0)
        builder.append("]s")
        try {
            val uptime = ManagementFactory.getRuntimeMXBean().uptime
            builder.append(", JVM运行时间为[${uptime / 1000.0}]s")
        } catch (ignored: Exception) {
            // No JVM Running Info, ignored
        }
        return builder.toString()
    }

    /**
     * 获取SpringApplication的name, 如果sourceClass为空, 那么name默认为application; 如果sourceClass不为空, 则应用名称为简单类名
     *
     * @return applicationName
     */
    private fun getApplicationName(): String = sourceClass?.simpleName ?: "application"
}