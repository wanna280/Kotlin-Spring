package com.wanna.debugger.bistoury.instrument.client.classpath

import java.util.function.Supplier

/**
 * 应用程序的ClassPath的计算的Supplier
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/4
 *
 * @see DefaultAppClassPathSupplier
 */
fun interface AppClassPathSupplier : Supplier<List<String>>