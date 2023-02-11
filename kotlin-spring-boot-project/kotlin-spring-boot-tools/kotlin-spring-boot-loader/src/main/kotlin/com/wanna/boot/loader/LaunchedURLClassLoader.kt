package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive
import com.wanna.boot.loader.jar.Handler
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import javax.annotation.Nullable

/**
 * SpringBoot当中用于去加载SpringBoot应用当中的相关的类的ClassLoader
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
open class LaunchedURLClassLoader(
    val exploded: Boolean,
    val archive: Archive,
    val urls: Array<URL>,
    parent: ClassLoader
) : URLClassLoader(urls, parent) {

    @Nullable
    override fun findResource(name: String): URL? {
        if (exploded) {
            return super.findResource(name)
        }
        Handler.setUseFastConnectionExceptions(true)
        return try {
            super.findResource(name)
        } finally {
            Handler.setUseFastConnectionExceptions(false)
        }
    }

    @Throws(IOException::class)
    override fun findResources(name: String): Enumeration<URL> {
        if (exploded) {
            return super.findResources(name)
        }
        Handler.setUseFastConnectionExceptions(true)
        try {
            return UseFastConnectionExceptionsEnumeration(super.findResources(name))
        } finally {
            Handler.setUseFastConnectionExceptions(false)
        }
    }

    override fun loadClass(name: String): Class<*> {
        Handler.setUseFastConnectionExceptions(true)
        try {
            return super.loadClass(name)
        } finally {
            Handler.setUseFastConnectionExceptions(false)
        }
    }

    private class UseFastConnectionExceptionsEnumeration(private val delegate: Enumeration<URL>) :
        Enumeration<URL> {
        override fun hasMoreElements(): Boolean {
            Handler.setUseFastConnectionExceptions(true)
            return try {
                delegate.hasMoreElements()
            } finally {
                Handler.setUseFastConnectionExceptions(false)
            }
        }

        override fun nextElement(): URL {
            Handler.setUseFastConnectionExceptions(true)
            return try {
                delegate.nextElement()
            } finally {
                Handler.setUseFastConnectionExceptions(false)
            }
        }
    }
}