package com.wanna.framework.scheduling.config

/**
 * ScheduledTask的Holder，提供获取所有的ScheduledTask的方法
 *
 * @see com.wanna.framework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
 * @see ScheduledTaskRegistrar
 */
interface ScheduledTaskHolder {

    /**
     * 获取当前Holder当中维护的ScheduledTask的列表
     *
     * @return ScheduledTask列表
     */
    fun getScheduledTasks(): Set<ScheduledTask>
}