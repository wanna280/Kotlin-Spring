package com.wanna.boot.logging

import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import java.util.function.Supplier

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
interface DeferredLogFactory {

    /**
     * 获取到Deferred Logger
     *
     * @param destination destination
     * @return Deferred Logger
     */
    fun getLog(destination: Class<*>): Logger = getLog { LoggerFactory.getLogger(destination) }

    /**
     * 创建一个新的Deferred Logger
     *
     * @param destination 原始的Logger
     * @return 新的Deferred Logger
     */
    fun getLog(destination: Logger): Logger = getLog { destination }

    /**
     * 使用Supplier的方式去获取到Deferred Logger
     *
     * @param destination destination
     * @return Deferred Logger
     */
    fun getLog(destination: Supplier<Logger>): Logger
}