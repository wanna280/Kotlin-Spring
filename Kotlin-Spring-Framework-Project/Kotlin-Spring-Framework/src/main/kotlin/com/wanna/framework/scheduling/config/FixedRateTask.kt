package com.wanna.framework.scheduling.config

/**
 * 定时调度的固定速率任务
 */
class FixedRateTask(runnable: Runnable, interval: Long, initialDelay: Long) :
    IntervalTask(runnable, interval, initialDelay)