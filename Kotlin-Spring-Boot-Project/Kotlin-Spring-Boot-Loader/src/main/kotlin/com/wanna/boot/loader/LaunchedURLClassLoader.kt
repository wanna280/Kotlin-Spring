package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive
import java.net.URL
import java.net.URLClassLoader

/**
 * SpringBoot当中用于去加载SpringBoot应用当中的相关的类的ClassLoader
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
open class LaunchedURLClassLoader(val exploded: Boolean, val archive: Archive, urls: Array<URL>, parent: ClassLoader) :
    URLClassLoader(urls, parent) {

    override fun loadClass(name: String?): Class<*> {
        name ?: throw IllegalStateException("className不能为null")
        val resourcePath = name.replace(".", "/") + ".class"
        val stream = parent.getResourceAsStream(resourcePath) ?: throw ClassNotFoundException("无法找到类[$name]")
        try {
            val bytes = stream.readAllBytes()
            return defineClass(name, bytes, 0, bytes.size)
        } catch (ex: Exception) {
            return super.loadClass(name)
        } finally {
            stream.close()
        }
    }
}