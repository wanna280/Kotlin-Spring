package com.wanna.framework.scheduling.annotation

import com.wanna.framework.context.annotation.Import

/**
 * 开启Spring的定时任务的支持, 导入处理定时任务相关的处理器; 
 * 可以在一个SpringBean的方法上标注@Scheduled注解标识该方法需要使用定时任务去进行运行
 *
 * 使用方式如下, 要开启定时任务的调度, 必须保证该Bean被Spring所管理：
 *
 * ```kotlin
 *     @Component
 *     @EnableScheduling
 *     class App {
 *
 *     }
 * ```
 *
 * @see Scheduled
 */
@Target(AnnotationTarget.CLASS)
@Import([SchedulingConfiguration::class])
annotation class EnableScheduling
