package com.wanna.nacos.api.config.listener

import java.util.concurrent.Executor

/**
 * 抽象的Listener的实现, 将[Executor]设置为null
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
abstract class AbstractListener : Listener {
    override fun getExecutor(): Executor? = null
}