package com.wanna.middleware.cli

import com.wanna.middleware.cli.annotation.CLIConfigurator
import com.wanna.middleware.cli.impl.DefaultCLI

/**
 * 提供对于[CLI]的创建的工厂方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 *
 * @see CLI
 */
object CLIs {

    /**
     * 根据给定的命令名, 去创建[CLI]
     *
     * @param name command name
     * @return CLI
     */
    @JvmStatic
    fun create(name: String): CLI {
        return DefaultCLI().setName(name)
    }

    @JvmStatic
    fun create(clazz: Class<*>): CLI {
        return CLIConfigurator.define(clazz)
    }
}