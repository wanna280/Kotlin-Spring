package com.wanna.boot

/**
 * 这是一个注册配置的[BootstrapContext]，通过继承[BootstrapRegistry]去提供实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/5/1
 * @see BootstrapRegistry
 * @see BootstrapContext
 * @see DefaultBootstrapContext
 */
interface ConfigurableBootstrapContext : BootstrapContext, BootstrapRegistry