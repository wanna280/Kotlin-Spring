package com.wanna.framework.core.task

/**
 * 提供对一个Runnable任务去进行包装的装饰器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/19
 */
fun interface TaskDecorator {
    fun decorate(runnable: Runnable): Runnable
}