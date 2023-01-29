package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * 基于[ByteArray]去实现的[Resource]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 *
 * @param byteArray 数据读取的ByteArray
 * @param description 资源的描述信息
 */
open class ByteArrayResource(private val byteArray: ByteArray, @Nullable private val description: String?) :
    AbstractResource() {

    /**
     * 提供一个只需要[ByteArray]的构造器
     *
     * @param byteArray ByteArray
     */
    constructor(byteArray: ByteArray) : this(byteArray, "resource loaded from byte array")

    override fun exists(): Boolean = true

    /**
     * [byteArray]当中的内容的长度
     *
     * @return byte array size
     */
    override fun contentLength(): Long = this.byteArray.size.toLong()

    /**
     * 获取输入流
     *
     * @return InputStream of ByteArray
     */
    override fun getInputStream(): InputStream = ByteArrayInputStream(byteArray)

    override fun getDescription(): String? = description

    /**
     * equals, 使用[byteArray]的内容去进行生成
     *
     * @param other other
     */
    override fun equals(@Nullable other: Any?): Boolean {
        if (this === other) return true
        return other is ByteArrayResource && byteArray.contentEquals(other.byteArray)
    }

    /**
     * hashCode, 使用[byteArray]的内容去进行生成
     *
     * @return hashCode
     */
    override fun hashCode(): Int {
        return byteArray.contentHashCode()
    }


}