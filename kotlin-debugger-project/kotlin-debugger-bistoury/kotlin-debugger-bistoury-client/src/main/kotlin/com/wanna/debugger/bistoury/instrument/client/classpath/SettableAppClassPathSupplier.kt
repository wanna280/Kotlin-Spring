package com.wanna.debugger.bistoury.instrument.client.classpath

/**
 * 可以去进行手动设置的AppClassPath的Supplier
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/4
 *
 * @param classpath 需要使用的ClassPath路径列表
 */
class SettableAppClassPathSupplier(private val classpath: List<String>) : AppClassPathSupplier {
    override fun get(): List<String> = classpath
}