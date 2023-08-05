package com.wanna.nacos.config.server.manager

import com.wanna.nacos.api.common.task.engine.NacosDelayTaskExecuteEngine

/**
 * TaskManager, 负责异步执行任务, 具体使用时, 需要先初始化[com.wanna.nacos.api.common.task.NacosTaskProcessor]才能使用
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 * @param processInterval 执行任务的间隔时间(单位为ms, 默认为100ms)
 * @param name 执行引擎的name
 */
open class TaskManager(name: String, processInterval: Long = 100L) : NacosDelayTaskExecuteEngine(name, processInterval)