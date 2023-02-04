package com.wanna.debugger.bistoury.instrument.client.common

import java.util.concurrent.locks.Lock

/**
 * 存放类对应的Class文件的字节码的缓存
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @see DefaultClassFileBuffer
 */
interface ClassFileBuffer {

    /**
     * 从缓存当中获取给定的类的字节码
     *
     * @param clazz clazz
     * @param defaultBuffer 找不到时要默认使用的字节码
     * @return 该类对应的字节码(如果找到了, 使用缓存当中的字节码数据; 如果没有找到的话, 那么返回给定的默认字节码数据)
     */
    fun getClassBuffer(clazz: Class<*>, defaultBuffer: ByteArray): ByteArray

    /**
     * 保存给定的类的字节码到缓存当中
     *
     * @param clazz clazz
     * @param buffer 该类要进行存储的的字节码
     */
    fun setClassBuffer(clazz: Class<*>, buffer: ByteArray)

    /**
     * 获取操作缓存的锁
     *
     * @return Lock
     */
    fun getLock(): Lock

    /**
     * 清空当前缓存当中已经缓存的字节码数据
     */
    fun destroy()

}