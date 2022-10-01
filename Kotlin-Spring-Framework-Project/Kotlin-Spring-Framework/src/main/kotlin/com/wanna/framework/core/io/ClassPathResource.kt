package com.wanna.framework.core.io

import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ResourceUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.nio.file.Files

/**
 * ClassPath的资源，基于类加载器去加载资源
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 */
open class ClassPathResource
private constructor(_path: String, @Nullable _classLoader: ClassLoader?, @Nullable _clazz: Class<*>?) :
    AbstractFileResolvingResource(), WritableResource {

    /**
     * 提供一个基于clazz的构建方法
     */
    constructor(_path: String, _clazz: Class<*>) : this(_path, null, _clazz)

    /**
     * 提供一个基于classLoader的构建方法
     */
    constructor(_path: String, classLoader: ClassLoader?) : this(_path, classLoader, null)

    /**
     * 获取
     */
    private val clazz: Class<*>? = _clazz

    /**
     * 如果必要的话，需要把ClassPath后的第一个"/"去掉
     */
    private val path: String = if (_path.startsWith("/")) _path.substring(1) else _path

    /**
     * 如果用户没有指定ClassLoader的话，那么我们使用默认的ClassLoader
     */
    private val classLoader: ClassLoader = _classLoader ?: ClassUtils.getDefaultClassLoader()

    override fun getInputStream(): InputStream {
        val stream = if (clazz != null) clazz.getResourceAsStream(path) else classLoader.getResourceAsStream(path)
        return stream
            ?: throw FileNotFoundException("[${getDescription()}]资源无法解析成为InputStream，因为该资源并不存在")
    }

    override fun getURL(): URL {
        val url = if (clazz != null) clazz.getResource(path) else classLoader.getResource(path)
        return url ?: throw FileNotFoundException("[${getDescription()}]资源无法被解析成为URL，因为该资源并不存在")
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
        val builder = StringBuilder("Class Path Resource [")
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

    open fun getPath() : String = this.path

    override fun getOutputStream() : OutputStream = Files.newOutputStream(getFile().toPath())
}