package com.wanna.debugger.bistoury.instrument.client.classpath

import com.wanna.debugger.bistoury.instrument.client.common.store.BistouryStore
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import javax.annotation.Nullable

/**
 * Jar包存储位置的相关工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/4
 */
object JarStorePathUtils {

    /**
     * Jar包的存放位置
     */
    @JvmStatic
    private val STORE_PATH = BistouryStore.getStorePath("bistoury_webapp")

    /**
     * 配置JarLib的存放位置的系统属性Key
     */
    private const val JAR_LIB_PATH_CONFIG_KEY = "bistoury.jar.lib.path"

    /**
     * 配置项目源码对应的字节码的存放位置的系统属性Key
     */
    private const val JAR_CLASSES_PATH_CONFIG_KEY = "bistoury.jar.source.path"

    /**
     * SpringBoot的Jar包存放位置的Manifest Key
     */
    private const val SPRING_BOOT_LIB_MANIFEST_KEY = "Spring-Boot-Lib"

    /**
     * SpringBoot的项目源码对应的字节码的存放位置的Manifest Key
     */
    private const val SPRING_BOOT_CLASSES_MANIFEST_KEY = "Spring-Boot-Classes"

    /**
     * 默认的SpringBoot的Manifest信息, libPath="/BOOT-INF/lib", classesPath="/BOOT-INF/classes"
     */
    @JvmStatic
    private val DEFAULT_SPRING_BOOT_MANIFEST = SpringBootManifest("/BOOT-INF/lib", "/BOOT-INF/classes")

    /**
     * SpringBoot Manifest
     */
    @JvmStatic
    @Nullable
    private var springBootManifest: SpringBootManifest? = null

    /**
     * 获取Jar包存储的位置
     *
     * @return jarStorePath
     */
    @JvmStatic
    fun getJarStorePath(): String = File(STORE_PATH).path

    /**
     * 获取当前应用的Jar包的Lib依赖的存放位置
     *
     * @return jarLibPath
     */
    @JvmStatic
    fun getJarLibPath(): String = System.getProperty(
        JAR_LIB_PATH_CONFIG_KEY,
        File(getJarStorePath(), getSpringBootManifest().libPath).path
    )

    /**
     * 获取Jar包当中的项目类的字节码的存放位置
     *
     * @return jarClassesPath
     */
    @JvmStatic
    fun getJarClassesPath(): String = System.getProperty(
        JAR_CLASSES_PATH_CONFIG_KEY,
        File(getJarStorePath(), getSpringBootManifest().classesPath).path
    )

    /**
     * 获取SpringBoot的Manifest信息, 用于获取到lib和classes目录
     *
     * @return SpringBoot Manifest
     */
    private fun getSpringBootManifest(): SpringBootManifest {
        if (this.springBootManifest == null) {
            val manifestFiles = File(getJarStorePath()).listFiles(FileFilter { "MANIFEST.SF" == it.name })
            if (manifestFiles.isNullOrEmpty()) {
                this.springBootManifest = DEFAULT_SPRING_BOOT_MANIFEST
            } else {
                for (manifestFile in manifestFiles) {
                    val manifestFileMap = readManifestFileAsMap(manifestFile)
                    val lib = manifestFileMap[SPRING_BOOT_LIB_MANIFEST_KEY]
                    val classes = manifestFileMap[SPRING_BOOT_CLASSES_MANIFEST_KEY]
                    if (!lib.isNullOrBlank() && !classes.isNullOrBlank()) {
                        this.springBootManifest = SpringBootManifest(lib, classes)
                        break
                    }
                }
            }
        }
        return this.springBootManifest ?: throw IllegalStateException("manifest is not available")
    }


    private fun readManifestFileAsMap(manifestFile: File): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        FileInputStream(manifestFile).use {
            val fileContent = String(it.readAllBytes(), StandardCharsets.UTF_8)
            val lines = fileContent.split("\n")
            for (line in lines) {
                val lineKeyValue = line.split(":")
                if (lineKeyValue.size != 2) {
                    continue
                }
                val key = lineKeyValue[0]
                val value = lineKeyValue[1]
                result[key] = value
            }
        }
        return result
    }

    /**
     * 解析SpringBoot的"Spring-Boot-Lib"和"Spring-Boot-Classes"的Manifest的结果
     *
     * @param libPath Jar包依赖的存放相对路径
     * @param classesPath 项目类源码的字节码的存放相对路径
     */
    private data class SpringBootManifest(val libPath: String, val classesPath: String)

}