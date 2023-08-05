package com.wanna.boot.loader.tools

import java.io.IOException
import java.io.InputStream

/**
 *
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
interface LoaderClassesWriter {

    /**
     * 写入默认的SpringBootLoader的相关类到Jar包当中
     *
     * @throws IOException 如果写入默认的SpringBootLoader类失败
     */
    @Throws(IOException::class)
    fun writeLoaderClasses()

    /**
     * 写入自定义的SpringBootLoader的相关类到Jar包当中
     *
     * @param loaderJarResourceName 需要写入到Jar包当中的资源
     * @throws IOException 如果写入自定义的SpringBootLoader类失败
     */
    @Throws(IOException::class)
    fun writeLoaderClasses(loaderJarResourceName: String)

    /**
     * 写入单个Entry到Jar包当中
     *
     * @param name entryName
     * @param inputStream 该entryName内容的读取的输入流
     * @throws IOException 如果写入Entry失败
     */
    @Throws(IOException::class)
    fun writeEntry(name: String, inputStream: InputStream)
}