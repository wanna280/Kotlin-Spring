package com.wanna.framework.test.context

import com.wanna.framework.context.ApplicationContext

/**
 * 提供基于Spring的XML配置文件, 去加载得到[ApplicationContext]的ContextLoader
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
interface ContextLoader {

    /**
     * 对于给定的[locations]去进行处理
     *
     * @param locations locations
     * @return 处理之后的locations
     */
    fun processLocations(vararg locations: String): Array<String>

    /**
     * 根据给定的[locations]去加载得到[ApplicationContext]
     *
     * @param locations locations
     * @return ApplicationContext
     */
    fun loadContext(vararg locations: String): ApplicationContext
}