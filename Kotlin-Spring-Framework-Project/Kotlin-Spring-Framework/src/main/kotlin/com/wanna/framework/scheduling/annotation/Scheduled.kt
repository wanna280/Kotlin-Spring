package com.wanna.framework.scheduling.annotation

/**
 * 标识这是一个定时任务的方法，它需要被标注在要做定时任务的方法上，Note: 该方法必须在Spring Bean上才有效；
 * 使用方法和juc当中的`ScheduledExecutorService`完全类似，也会有fixedDelay/fixedRate两种类型的定时任务；
 *
 * * 1.fixedDelay模式，固定第一次任务结束之后-第二次任务之间的时间，两次任务间隔时间=`delay + 任务的耗时`
 * * 2.fixedRate模式，固定两次任务的开始之间的时间，两次任务的间隔时间为`Math.max(delay, 任务的耗时)`
 *
 * 在下面这种情况(fixedDelay)下，两次任务的延时时间应该为8s(5+3=8s)
 *
 * ```kotlin
 *     @Scheduled(initialDelay = 5000L, fixedDelay = 5000L)
 *     open fun schedule() {
 *         println(Date(System.currentTimeMillis()))
 *         TimeUnit.SECONDS.sleep(3L)
 *     }
 * ```
 *
 * 在下面这种情况(fixedRate)下，两次任务之间的延时时间应该为5s(`max(3,5)=5`)
 *
 * ```kotlin
 *     @Scheduled(initialDelay = 5000L, fixedRate = 5000L)
 *     open fun schedule() {
 *         println(Date(System.currentTimeMillis()))
 *         TimeUnit.SECONDS.sleep(3L)
 *     }
 * ```
 *
 * 但是，在下面这种情况下，就算是fixedRate模式下也得等10s，因为10s>5s，
 * 应该等待的时间为`max(10,5)=10`，因为必须等当前的任务执行完成之后才能重新调度该任务
 *
 * ```kotlin
 *     @Scheduled(initialDelay = 5000L, fixedRate = 5000L)
 *     open fun schedule() {
 *         println(Date(System.currentTimeMillis()))
 *         TimeUnit.SECONDS.sleep(10L)
 *     }
 * ```
 *
 * @param initialDelay 定时任务初始delay时间，可以配合fixedDelay/fixedRate使用，不配置，默认为0
 * @param fixedDelay 定时任务的固定delay
 * @param fixedDelayString 定时任务的固定delay(支持嵌入式值解析)
 * @param fixedRate 定时任务的固定速率
 * @param fixedRateString 定时任务的固定速率(支持嵌入式值解析)
 *
 * @see java.util.concurrent.ScheduledExecutorService
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Scheduled(
    val initialDelay: Long = -1,
    val fixedDelay: Long = -1,
    val fixedDelayString: String = "",
    val fixedRate: Long = -1,
    val fixedRateString: String = ""
)
