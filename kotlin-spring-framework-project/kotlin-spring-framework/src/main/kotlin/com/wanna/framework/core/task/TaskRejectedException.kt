package com.wanna.framework.core.task

import java.util.concurrent.RejectedExecutionException

/**
 * 任务被线程池拒绝的异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/20
 *
 * @param message error message
 * @param cause 被拒绝的原因
 * @see com.wanna.framework.scheduling.concurrent.ThreadPoolTaskScheduler
 * @see com.wanna.framework.scheduling.concurrent.ThreadPoolTaskExecutor
 */
open class TaskRejectedException(message: String? = null, cause: Throwable? = null) : RejectedExecutionException()