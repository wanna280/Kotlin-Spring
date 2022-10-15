package com.wanna.boot

/**
 * 这是一个Bootstrapper，支持去对BootstrapRegistry去进行初始化
 *
 * @see BootstrapRegistry
 * @see ConfigurableBootstrapContext
 */
interface Bootstrapper {

    /**
     * 对BootstrapRegistry去进行初始化
     *
     * @param bootstrapRegistry BootstrapRegistry
     */
    fun initialize(bootstrapRegistry: BootstrapRegistry)
}