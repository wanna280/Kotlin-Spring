package com.wanna.framework.core.io

import java.io.OutputStream
import java.nio.channels.Channels
import java.nio.channels.WritableByteChannel

/**
 * 可以去进行写的资源
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 */
interface WritableResource : Resource {

    /**
     * 当前资源是否可以写？
     */
    fun isWritable(): Boolean

    /**
     * 如果该资源可以写的话, 那么应该可以去获取到OutputStream
     *
     * @return OutputStream
     */
    fun getOutputStream(): OutputStream

    /**
     * 获取一个可以去进行写的Channel
     *
     * @return WritableByteChannel
     */
    fun writableChannel(): WritableByteChannel = Channels.newChannel(getOutputStream())
}