package com.wanna.boot.loader.jar

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.security.Permission

/**
 * 描述了一个Jar包的相关洗洗脑
 * 这是一个抽象的JarFile的实现，直接去继承java.util.jar包下的JarFile；
 * 为SpringBootLoader当中的JarFile的实现提供基础
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 * @see java.util.jar.JarFile
 * @see JarFile
 */
abstract class AbstractJarFile(file: File) : java.util.jar.JarFile(file) {

    /**
     * 获取当前JarFile的URL
     *
     * @return 当前JarFile的URL
     * @throws MalformedURLException 如果Jar包不合法
     */
    @Throws(MalformedURLException::class)
    abstract fun getUrl(): URL

    /**
     * 获取当前JarFile的输入流
     *
     * @return JarFile的输入流
     * @throws IOException 如果找不到该Jar包文件的话
     */
    @Throws(IOException::class)
    abstract fun getInputStream(): InputStream

    /**
     * 获取当前的JarFile的类型
     *
     * @return 当前JarFile的类型
     */
    abstract fun getJarFileType(): JarFileType

    /**
     * 获取当前JarFile的Permission
     *
     * @return 当前JarFile的Permission
     */
    abstract fun getPermission(): Permission

    /**
     * 描述了JarFile的类型
     */
    enum class JarFileType {
        DIRECT, NESTED_DIRECTORY, NESTED_JAR
    }

}