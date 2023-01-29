package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ResourceUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.nio.file.Files

/**
 * 基于ClassPath作为路径, 去进行[Resource]的资源实现, 基于Java的[ClassLoader]类加载器去加载资源
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 *
 * @param path 要去进行加载的资源路径classpath
 * @param classLoader 执行资源的加载ClassLoader
 * @param clazz 执行资源的加载的类(将会使用该类的ClassLoader去进行加载),
 * * 1.如果指定的是相对路径(例如"test.txt"), 那么会以该类的所在的包去作为baseDir, 去进行**相对路径**的资源寻找
 * * 2.如果指定的是绝对路径, 例如"/a/b/test.txt", 那么就是直接使用ClassLoader去进行资源的寻找, 不会拼接baseDir
 */
open class ClassPathResource(
    path: String,
    @Nullable classLoader: ClassLoader?,
    @Nullable private val clazz: Class<*>?
) : AbstractFileResolvingResource(), WritableResource {

    /**
     * 提供一个基于Class的构建方法, 资源加载时将会使用该类的ClassLoader去进行加载
     *
     * @param path 资源路径classpath(可以是相对clazz的相对路径, 也可以是以"/"开头的classpath绝对路径)
     * @param clazz 进行资源加载的类
     */
    constructor(path: String, clazz: Class<*>) : this(path, null, clazz)

    /**
     * 提供一个基于classLoader的构建方法
     *
     * @param path 资源路径classpath
     * @param classLoader 要去进行使用资源的加载时使用的类加载器(为null时将会使用默认的ClassLoader)
     */
    constructor(path: String, @Nullable classLoader: ClassLoader?) : this(path, classLoader, null)

    /**
     * 提供一个只给定path的构建方法, 使用默认的[ClassLoader]去进行[Resource]加载
     *
     * @param path 资源路径classpath
     */
    constructor(path: String) : this(path, null)

    /**
     * 如果必要的话, 需要把ClassPath后的第一个"/"去掉
     */
    private val path = if (path.startsWith("/")) path.substring(1) else path

    /**
     * 如果没有明确地去指定ClassLoader的话, 那么我们使用默认的ClassLoader进行资源加载
     */
    @Nullable
    private var classLoader: ClassLoader? = classLoader ?: ClassUtils.getDefaultClassLoader()

    /**
     * 获取当前[ClassPathResource]资源的输入流[InputStream], 进行内容的读取
     *
     * @return InputStream of ClassPath Resource
     *
     * @see ClassLoader.getResourceAsStream
     * @see Class.getResourceAsStream
     * @see ClassLoader.getSystemResourceAsStream
     */
    override fun getInputStream(): InputStream {
        // 如果给定的是Class的话, 那么以Class所在的包的位置去作为baseDir去进行资源的寻找
        val stream = if (clazz != null) {
            clazz.getResourceAsStream(path)

            // 如果给定的是ClassLoader的话, 那么将会尝试使用ClassLoader.getResourcesAsStream去进行资源的寻找
        } else if (classLoader != null) {
            classLoader!!.getResourceAsStream(path)

            // 如果啥也没给的话, 那么将会使用ClassLoader.getSystemResourceAsStream去进行获取
        } else {
            ClassLoader.getSystemResourceAsStream(path)
        }
        return stream
            ?: throw FileNotFoundException("${getDescription()} cannot be opened because it does not exist")
    }

    /**
     * 对于exists的判断, 不能沿用File的判断, 这里去进行override, 只要resolveURL的结果不为空计算是存在
     *
     * @return 如果可以解析到URL, return true; 否则return false
     */
    override fun exists(): Boolean = resolveURL() != null

    /**
     * 获取到当前[ClassPathResource]的资源URL
     *
     * @return URL of this ClassPathResource
     * @throws FileNotFoundException 如果该ClassPathResource不存在
     */
    override fun getURL(): URL {
        return resolveURL()
            ?: throw FileNotFoundException("${getDescription()} cannot be resolved as URL, because it does not exist")
    }

    /**
     * 解析得到当前[ClassPathResource]资源所在的[URL]
     *
     * @return URL(如果无法解析的话, return null)
     */
    @Nullable
    private fun resolveURL(): URL? {
        if (this.clazz != null) {
            return clazz.getResource(path)
        } else if (classLoader != null) {
            return classLoader!!.getResource(path)
        } else {
            return ClassLoader.getSystemResource(path)
        }
    }

    /**
     * 创建一个以当前[Resource]作为基准路径的相对位置的[ClassPathResource]
     *
     * @param relativePath 相对当前资源的路径
     * @return 根据relativePath去解析到的[ClassPathResource]
     */
    override fun createRelative(relativePath: String): Resource {
        // 根据给定的relativePath, 先去生成相对路径
        val pathToUse = ResourceUtils.applyRelativePath(path, relativePath)

        // 创建ClassPathResource
        return if (clazz != null) ClassPathResource(pathToUse, clazz)
        else ClassPathResource(pathToUse, this.classLoader)
    }

    /**
     * 获取当前[ClassPathResource]资源的描述信息
     *
     * @return 当前ClassPath的资源的描述信息
     */
    override fun getDescription(): String = "class path resource [$path]"

    /**
     * 当前[ClassPathResource]是否可写?
     */
    override fun isWritable(): Boolean {
        return try {
            val file = getFile()
            file.canWrite() && !file.isDirectory
        } catch (ex: IOException) {
            false
        }
    }

    open fun getPath(): String = this.path

    override fun getOutputStream(): OutputStream = Files.newOutputStream(getFile().toPath())

    /**
     * 采用clazz&path&classLoader去进行生成equals的比较
     *
     * @param other other
     */
    override fun equals(@Nullable other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClassPathResource) {
            return false
        }

        if (clazz != other.clazz) return false
        if (path != other.path) return false
        if (classLoader != other.classLoader) return false

        return true
    }

    /**
     * 采用clazz&path&classLoader去进行hashCode的生成
     *
     * @return hashCode
     */
    override fun hashCode(): Int {
        var result = clazz?.hashCode() ?: 0
        result = 31 * result + path.hashCode()
        result = 31 * result + classLoader.hashCode()
        return result
    }
}