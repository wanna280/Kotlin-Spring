package com.wanna.boot.loader.jar

import java.io.File
import java.io.InputStream
import java.net.URL
import java.security.Permission
import java.util.jar.JarEntry

/**
 * SpringBootLoader当中对于JarFile的实现，继承自java.util.jar包下的JarFile
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 * @see java.util.jar.JarFile
 */
open class JarFile(file: File) : AbstractJarFile(file), Iterable<java.util.jar.JarEntry> {

    override fun getUrl(): URL {
        TODO("Not yet implemented")
    }

    override fun getInputStream(): InputStream {
        TODO("Not yet implemented")
    }

    override fun getJarFileType(): JarFileType {
        TODO("Not yet implemented")
    }

    override fun getPermission(): Permission {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<JarEntry> {
        TODO("Not yet implemented")
    }
}