package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
import java.io.InputStream

/**
 * 基于[InputStream]去实现[Resource]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 *
 * @param inputStream InputStream
 * @param description 资源的描述信息
 */
open class InputStreamResource(private val inputStream: InputStream, @Nullable private val description: String?) :
    AbstractResource() {

    /**
     * 提供一个只需要[InputStream]的构造器
     *
     * @param inputStream InputStream
     */
    constructor(inputStream: InputStream) : this(inputStream, "")

    /**
     * 是否已经读取过的标志位
     */
    private var read = false

    /**
     * 获取[InputStream], 只允许获取一次, 获取获取[InputStream]将会丢出[IllegalStateException]异常
     *
     * @return InputStream
     */
    override fun getInputStream(): InputStream {
        if (read) {
            throw IllegalStateException("InputStream has already been read - do not use InputStreamResource if a stream needs to be read multiple times")
        }
        this.read = true
        return inputStream
    }

    override fun isOpen(): Boolean = true

    override fun exists(): Boolean = true

    override fun getDescription(): String = "InputStream resource [$description]"

    override fun equals(@Nullable other: Any?): Boolean {
        return this === other || (other is InputStreamResource && other.inputStream == this.inputStream)
    }

    override fun hashCode(): Int {
        return inputStream.hashCode()
    }
}