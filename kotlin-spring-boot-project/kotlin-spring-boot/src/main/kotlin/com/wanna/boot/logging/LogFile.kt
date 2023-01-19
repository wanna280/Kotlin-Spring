package com.wanna.boot.logging

import com.wanna.framework.core.environment.PropertyResolver
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.StringUtils
import java.io.File
import java.util.*

/**
 * 对于[LoggingSystem]的日志文件的[filePath]和[fileName]去进行封装, 可以通过"logging.file.name"
 * 和"logging.file.path"这两个配置, 去指定日志文件的输出地址,
 *
 * Note: 如果没有通过"logging.file.name"去指定配置文件的"fileName"的话, 那么将会使用"spring.log"作为日志文件的文件名
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @param fileName 日志文件名
 * @param filePath 日志文件路径
 */
open class LogFile(@Nullable val fileName: String?, @Nullable private val filePath: String?) {

    companion object {

        /**
         * 日志文件名的属性Key
         */
        const val FILE_NAME_PROPERTY = "logging.file.name"

        /**
         * 日志文件的路径属性Key
         */
        const val FILE_PATH_PROPERTY = "logging.file.path"

        /**
         * 默认的日志文件的文件名(当只是指定了filePath的情况下使用)
         */
        const val DEFAULT_FILE_NAME = "spring.log"

        /**
         * 从给定的[PropertyResolver]当中去获取到日志文件的path/fileName
         *
         * @param propertyResolver PropertyResolver
         * @return 解析到的LogFile配置信息(如果没有去进行特殊的配置, 那么return null)
         */
        @Nullable
        @JvmStatic
        fun get(propertyResolver: PropertyResolver): LogFile? {
            val fileName = propertyResolver.getProperty(FILE_NAME_PROPERTY)
            val filePath = propertyResolver.getProperty(FILE_PATH_PROPERTY)

            // 只有配置了filePath/fileName之中的一个或者两个, 才去构建出来LogFile
            if (StringUtils.hasLength(fileName) || StringUtils.hasLength(filePath)) {
                return LogFile(fileName, filePath)
            }
            // 如果一个都没配置, 那么return null
            return null
        }
    }

    init {
        // 如果两个都没有指定的话, 那么丢异常出来...
        if (!StringUtils.hasLength(filePath) && !StringUtils.hasLength(fileName)) {
            throw IllegalStateException("File or Path must not be empty")
        }
    }

    /**
     * 将当前[LogFile]配置信息, 应用给SystemProperties当中
     *
     * @see System.getProperties
     */
    open fun applyToSystemProperties() {
        applyTo(System.getProperties())
    }

    /**
     * 将[LogFile]当中的配置信息, 去apply给给定的[Properties]对象当中
     *
     * @param properties Properties
     */
    open fun applyTo(properties: Properties) {
        put(properties, LoggingSystemProperties.LOG_PATH, this.filePath)
        put(properties, LoggingSystemProperties.LOG_FILE, toString())
    }

    private fun put(properties: Properties, key: String, value: String?) {
        if (StringUtils.hasLength(value)) {
            properties[key] = value
        }
    }

    /**
     * LogFile toString
     *
     * @return toString
     */
    override fun toString(): String {
        if (StringUtils.hasLength(fileName)) {
            return fileName!!
        }
        return File(this.filePath, DEFAULT_FILE_NAME).path
    }
}