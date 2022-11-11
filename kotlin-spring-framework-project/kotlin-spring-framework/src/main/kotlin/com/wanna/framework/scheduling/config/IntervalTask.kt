package com.wanna.framework.scheduling.config

/**
 * 定时调度的间隔任务
 *
 * @param initialDelay 初始化延时时间
 * @param interval 间隔时间
 * @param runnable 要执行的任务
 */
open class IntervalTask(runnable: Runnable, val interval: Long, val initialDelay: Long) : Task(runnable)