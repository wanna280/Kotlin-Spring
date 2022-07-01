package com.wanna.framework.scheduling.config

/**
 * 定时调度的固定间隔的任务
 */
open class FixedDelayTask(runnable: Runnable, interval: Long, initialDelay: Long) :
    IntervalTask(runnable, interval, initialDelay)