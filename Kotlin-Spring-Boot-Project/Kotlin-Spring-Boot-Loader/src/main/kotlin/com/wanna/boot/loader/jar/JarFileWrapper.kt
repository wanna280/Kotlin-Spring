package com.wanna.boot.loader.jar

import java.io.InputStream
import java.util.function.Function
import java.util.jar.JarEntry
import java.util.zip.ZipEntry

/**
 * JarFile的Wrapper，通过包装一个JarFile去委托执行相关的方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/3
 */
class JarFileWrapper(private val parent: JarFile) : AbstractJarFile(parent.rootJarFile.file) {
    override fun getUrl() = parent.getUrl()
    override fun getInputStream() = parent.getInputStream()
    override fun getJarFileType() = parent.getJarFileType()
    override fun getPermission() = parent.getPermission()
    override fun getJarEntry(name: String): JarEntry = parent.getJarEntry(name)
    override fun getInputStream(ze: ZipEntry): InputStream = parent.getInputStream(ze)
    override fun getType() = parent.getType()
}