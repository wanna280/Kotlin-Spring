package com.wanna.middleware.cli

import com.wanna.middleware.cli.annotation.CLIConfigurator
import com.wanna.middleware.cli.annotation.Name
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
     * 根据给定的命令名, 去创建出来一个默认的[CLI], 只存在有commandName属性,
     * 别的属性值都是默认值, 待进行后续的填充
     *
     * @param name command name
     * @return CLI
     */
    @JvmStatic
    fun create(name: String): CLI {
        return DefaultCLI().setName(name)
    }

    /**
     * 根据给定的类, 从该类当中去提取[Argument]和[Option]等相关的注解, 并封装成为[CLI]实例对象
     *
     * * 1.对于commandName, 将会采用[Name]注解的value属性
     * * 2.对于options/arguments信息, 都是从[Argument]和[Option]等注解当中去进行提取的
     *
     * @param clazz clazz
     * @return CLI
     */
    @JvmStatic
    fun create(clazz: Class<*>): CLI {
        return CLIConfigurator.define(clazz)
    }
}