package com.wanna.debugger.bistoury.instrument.client.classpath

import java.util.function.Supplier

/**
 * 应用程序的LibClass的Supplier
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/4
 */
fun interface AppLibClassSupplier : Supplier<Class<*>>