package com.wanna.nacos.api.core

import com.wanna.nacos.api.common.executor.NameThreadFactory
import java.util.concurrent.Executors

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
object GlobalExecutor {

    @JvmStatic
    private val COMMON_EXECUTOR = Executors.newScheduledThreadPool(4, NameThreadFactory("com.wanna.nacos.core.common"))

    /**
     * 使用CommonExecutor去执行任务
     *
     * @param runnable 需要执行的任务Runnable
     */
    @JvmStatic
    fun executeByCommon(runnable: Runnable) {
        COMMON_EXECUTOR.execute(runnable)
    }
}