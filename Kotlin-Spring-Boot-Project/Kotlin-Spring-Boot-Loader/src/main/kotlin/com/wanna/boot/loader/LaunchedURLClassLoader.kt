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

}