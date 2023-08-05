package com.wanna.debugger.bistoury.instrument.client.common

import com.wanna.debugger.bistoury.instrument.client.classpath.AppClassPathSupplier
import com.wanna.debugger.bistoury.instrument.client.classpath.AppLibClassSupplier
import org.objectweb.asm.Type
import java.lang.instrument.Instrumentation
import java.util.concurrent.locks.Lock

/**
 * 封装[Instrumentation]、[Lock]、[ClassFileBuffer]的上下文信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param instrumentation JVM回调的Instrumentation
 * @param lock 锁
 * @param classFileBuffer Class文件的字节码缓存
 * @param appLibClassSupplier 应用程序的Jar包当中的类的计算的Supplier
 * @param appClassPathSupplier 应用程序的ClassPath的计算的Supplier
 */
open class InstrumentInfo(
    val instrumentation: Instrumentation,
    val lock: Lock,
    val classFileBuffer: ClassFileBuffer,
    private val appLibClassSupplier: AppLibClassSupplier,
    private val appClassPathSupplier: AppClassPathSupplier
) {

    /**
     * 已经完成转换的类, 操作时需要加锁保证线程安全
     */
    private val transformedClasses = LinkedHashSet<Class<*>>()

    /**
     * 获取应用程序的ClassPath
     *
     * @return classPath
     */
    open fun getClassPath(): List<String> = this.appClassPathSupplier.get()

    /**
     * 获取应用程序的LibClass
     *
     * @return Lib Class
     */
    open fun getLibClass(): Class<*> = this.appLibClassSupplier.get()

    /**
     * 根据给定的类签名去获取到对应的类
     *
     * @param signature 类的签名信息
     * @return 根据[Instrumentation]从所有已经加载的类当中去找到的合适的类
     */
    open fun signatureToClass(signature: String): Class<*> {
        val className = Type.getType(signature).className

        for (clazz in instrumentation.allLoadedClasses) {
            if (clazz.name == className) {
                return clazz
            }
        }
        throw ClassNotFoundException("Cannot find class $className")
    }

    open fun addTransformedClass(clazz: Class<*>) {
        lock.lock()
        try {
            transformedClasses += clazz
        } finally {
            lock.unlock()
        }
    }

}