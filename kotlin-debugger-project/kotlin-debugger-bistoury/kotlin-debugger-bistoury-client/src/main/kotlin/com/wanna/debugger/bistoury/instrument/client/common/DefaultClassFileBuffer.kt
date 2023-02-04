package com.wanna.debugger.bistoury.instrument.client.common

import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * [ClassFileBuffer]的默认实现, 是用于存放类的字节码的缓存
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
object DefaultClassFileBuffer : ClassFileBuffer {

    /**
     * 操作[ClassFileBuffer]的锁
     */
    @JvmStatic
    private val LOCK = ReentrantLock()

    /**
     * 类的字节码文件的缓存, Key-Class, Value-该Class对应的字节码
     */
    @JvmStatic
    private val CLASS_BYTE_CODES_CACHE = WeakHashMap<Class<*>, ByteArray>()

    /**
     * 从缓存当中获取给定的类的字节码
     *
     * @param clazz clazz
     * @param defaultBuffer 找不到时要默认使用的字节码
     * @return 该类对应的字节码(如果找到了, 使用缓存当中的字节码数据; 如果没有找到的话, 那么返回给定的默认字节码数据)
     */
    override fun getClassBuffer(clazz: Class<*>, defaultBuffer: ByteArray): ByteArray {
        val byteCodes = CLASS_BYTE_CODES_CACHE[clazz]
        if (byteCodes == null || byteCodes.isEmpty()) {
            return defaultBuffer
        }
        return byteCodes
    }

    /**
     * 保存给定的类的字节码到缓存当中
     *
     * @param clazz clazz
     * @param buffer 该类要进行存储的的字节码
     */
    override fun setClassBuffer(clazz: Class<*>, buffer: ByteArray) {
        CLASS_BYTE_CODES_CACHE[clazz] = buffer
    }

    /**
     * 获取操作[ClassFileBuffer]的锁
     *
     * @return Lock
     */
    override fun getLock(): Lock {
        return LOCK
    }

    override fun destroy() {
        CLASS_BYTE_CODES_CACHE.clear()
    }
}