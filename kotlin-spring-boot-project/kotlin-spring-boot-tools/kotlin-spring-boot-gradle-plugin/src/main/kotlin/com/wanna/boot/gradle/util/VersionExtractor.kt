package com.wanna.boot.gradle.util

import java.io.File
import java.net.JarURLConnection
import java.util.jar.Attributes
import java.util.jar.JarFile
import javax.annotation.Nullable

/**
 * 提取给定的类的版本信息的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
object VersionExtractor {

    /**
     * 获取给定的类的版本信息
     *
     * @param clazz Class
     * @return 提取到的版本信息, 提取不到版本信息的话return null
     */
    @Nullable
    @JvmStatic
    fun forClass(clazz: Class<*>): String? {

        val implementationVersion = clazz.`package`.implementationVersion
        if (implementationVersion != null) {
            return implementationVersion
        }
        // 获取该类的所在路径(目录/Jar包)
        val codeSourceLocation = clazz.protectionDomain.codeSource.location
        try {
            val urlConnection = codeSourceLocation.openConnection()

            // 获取该Jar包的Manifest当中的版本信息
            if (urlConnection is JarURLConnection) {
                return getImplementationVersion(urlConnection.jarFile)
            } else {
                JarFile(File(codeSourceLocation.toURI())).use {
                    return getImplementationVersion(it)
                }
            }
        } catch (ex: Exception) {
            return null
        }
    }

    /**
     * 从给定的Jar包的Manifest当中的"Implementation-Version"属性去获取版本
     *
     * @param jarFile JarFile
     * @return version(提取不到return null)
     */
    @Nullable
    @JvmStatic
    private fun getImplementationVersion(jarFile: JarFile): String? {
        return jarFile.manifest.mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)
    }

}