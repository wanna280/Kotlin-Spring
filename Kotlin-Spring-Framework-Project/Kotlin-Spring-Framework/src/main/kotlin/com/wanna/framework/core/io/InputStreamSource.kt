package com.wanna.framework.core.io

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * 它是一个提供InputStream的获取的简单接口
 *
 * 这个接口也是Spring的Resource的实现的基础接口
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 * @see InputStream
 * @see InputStreamSource
 */
interface InputStreamSource {

    /**
     * 获取InputStream
     *
     * @return InputStream
     * @throws FileNotFoundException 如果文件找不到的话
     * @throws IOException 如果该内容的流无法被打开的话
     */
    @Throws(FileNotFoundException::class, IOException::class)
    fun getInputStream(): InputStream
}