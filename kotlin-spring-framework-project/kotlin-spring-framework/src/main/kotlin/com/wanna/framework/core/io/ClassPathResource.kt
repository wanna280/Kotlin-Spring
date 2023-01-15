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
 * ClassPath的资源, 基于类加载器去加载资源
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 */
open class ClassPathResource
private constructor(path: String, @Nullable classLoader: ClassLoader?, @Nullable private val clazz: Class<*>?) :
    AbstractFileResolvingResource(), WritableResource {

    /**
     * 提供一个基于clazz的构建方法
     */
    constructor(path: String, clazz: Class<*>) : this(path, null, clazz)

    /**
     * 提供一个基于classLoader的构建方法
     */
    constructor(path: String, @Nullable classLoader: ClassLoader?) : this(path, classLoader, null)

    /**
     * 提供一个只给定path的构造器
     */
    constructor(path: String) : this(path, null)

    /**
     * 如果必要的话, 需要把ClassPath后的第一个"/"去掉
     */
    private val path = if (path.startsWith("/")) path.substring(1) else path

    /**
     * 如果用户没有指定ClassLoader的话, 那么我们使用默认的ClassLoader
     */
    @Nullable
    private var classLoader: ClassLoader? = classLoader ?: ClassUtils.getDefaultClassLoader()

    override fun getInputStream(): InputStream {
        val stream =
            if (clazz != null) {
                clazz.getResourceAsStream(path)
            } else if (classLoader != null) {
                classLoader!!.getResourceAsStream(path)
            } else {
                ClassLoader.getSystemResourceAsStream(path)
            }
        return stream
            ?: throw FileNotFoundException("[${getDescription()}]资源无法解析成为InputStream, 因为该资源并不存在")
    }

    /**
     * 对于exists的判断, 不能沿用File的判断, 这里去进行override, 只要resolveURL的结果不为空计算是存在
     *
     * @return 如果可以解析到URL, return true; 否则return false
     */
    override fun exists(): Boolean = resolveURL() != null

    /**
     * 获取到URL
     *
     * @return URL of ClassPathResource
     * @throws FileNotFoundException 如果该ClassPath的资源不存在
     */
    override fun getURL(): URL {
        val url = resolveURL()
        return url ?: throw FileNotFoundException("[${getDescription()}]资源无法被解析成为URL, 因为该资源并不存在")
    }

    /**
     * 根据ClassLoader去resolveURL
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
     * 实现创建一个相对路径方法
     *
     * @param relativePath 相对当前资源的路径
     * @return 根据相对路径解析到的资源
     */
    override fun createRelative(relativePath: String): Resource {
        val pathToUse = ResourceUtils.applyRelativePath(path, relativePath)
        return if (clazz != null) ClassPathResource(pathToUse, clazz)
        else ClassPathResource(pathToUse, this.classLoader)
    }

    /**
     * 获取资源的描述信息
     *
     * @return 当前ClassPath的资源的描述信息
     */
    override fun getDescription(): String {
        val builder = StringBuilder("class path resource [")
        if (clazz != null) {
            builder.append(clazz.name).append(".class")
        } else {
            builder.append(path)
        }
        builder.append("]")
        return builder.toString()
    }

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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassPathResource

        if (clazz != other.clazz) return false
        if (path != other.path) return false
        if (classLoader != other.classLoader) return false

        return true
    }

    /**
     * 采用clazz&path&classLoader去进行hashCode的生成
     */
    override fun hashCode(): Int {
        var result = clazz?.hashCode() ?: 0
        result = 31 * result + path.hashCode()
        result = 31 * result + classLoader.hashCode()
        return result
    }
}