package com.wanna.middleware.arthas.common

import java.util.*
import javax.annotation.Nullable

/**
 * JavaVersion的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
object JavaVersionUtils {

    private const val VERSION_PROPERTY_NAME = "java.specification.version"

    /**
     * 当前应用的Java版本
     */
    @JvmStatic
    private val JAVA_VERSION_STR: String? = System.getProperty(VERSION_PROPERTY_NAME)

    /**
     * 获取当前应用的Java版本
     *
     * @return JavaVersion
     */
    @Nullable
    @JvmStatic
    fun getJavaVersionStr(): String? = JAVA_VERSION_STR

    /**
     * 从给定的[Properties]当中, 提取到Java版本信息的属性值
     *
     * @param properties Properties
     * @return 从给定的Properties当中提前得到的javaVersion
     */
    @Nullable
    @JvmStatic
    fun getJavaVersionStr(properties: Properties): String? {
        return System.getProperty(VERSION_PROPERTY_NAME)
    }
}