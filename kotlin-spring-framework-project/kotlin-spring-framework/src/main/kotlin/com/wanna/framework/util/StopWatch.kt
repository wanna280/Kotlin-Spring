package com.wanna.framework.util

import java.util.concurrent.TimeUnit

/**
 * 这是一个秒表的工具，Note: 它不是线程安全的
 */
class StopWatch(_id: String) {
    constructor() : this("")

    // StopWatch ID
    private val id: String = _id

    // 当前任务的name，当为空时代表可以启动秒表，不为空时代表秒表有任务正在运行中
    private var currentTaskName: String? = null

    // 当前任务的开始时间
    private var startTimeNanos: Long = System.nanoTime()

    // 维护当前秒表运行过程当中的所有任务列表
    private val taskInfoList = ArrayList<TaskInfo>()

    // 总共的任务数量，当stop时会自动统计
    private var taskCount: Int = 0

    // 秒表的总计运行时间
    private var totalTimeNanos: Long = 0

    // 最后一个已经完成的任务的信息
    private var lastTaskInfo: TaskInfo? = null

    /**
     * 获取当前秒表当中的全部的任务的相关信息
     *
     * @return 当前秒表当中全部运行的任务的相关信息(taskName和运行时间)
     */
    fun getTaskInfo(): Array<TaskInfo> = this.taskInfoList.toTypedArray()

    /**
     * 获取当前秒表的最后一次任务的相关信息
     *
     * @return 最后一次任务的taskName以及运行时间
     * @throws IllegalStateException 如果之前还没有启动过就去进行获取
     */
    fun getLastTaskInfo(): TaskInfo = lastTaskInfo ?: throw IllegalStateException("当前秒表没有运行过任务，不能获取最后一个任务的相关信息")

    /**
     * 获取当前秒表的最后一次任务的taskName
     *
     * @return 最后一次任务的taskName
     * @throws IllegalStateException 如果之前还没有启动过就去进行获取
     */
    fun getLastTaskName(): String = lastTaskInfo?.name ?: throw IllegalStateException("当前秒表没有运行过任务，不能获取最后一个任务的taskName")

    /**
     * 获取秒表当中的任务数量
     *
     * @return 当前秒表的运行的任务的数量
     */
    fun getTaskCount(): Int = this.taskCount

    /**
     * 获取当前秒表的总计运行时间(单位为ns)
     *
     * @return 总计运行时间，单位为ns
     */
    fun getTotalTimeNanos(): Long = this.totalTimeNanos

    /**
     * 获取当前秒表的总计运行时间(单位为ms)
     *
     * @return 总计运行时间，单位为ms
     */
    fun getTotalTimeMills(): Long = TimeUnit.NANOSECONDS.toMillis(this.totalTimeNanos)

    /**
     * 获取当前秒表的总计运行时间(单位为s)
     *
     * @return 总计运行时间，单位为s
     */
    fun getTotalTimeSeconds(): Long = TimeUnit.NANOSECONDS.toSeconds(this.totalTimeNanos)

    /**
     * 开始一个任务，taskName为空
     */
    fun start() {
        this.start("")
    }

    /**
     * 获取秒表的ID
     *
     * @return 当前秒表Id
     */
    fun getId() = this.id

    /**
     * 判断当前秒表是否有任务正在执行当中
     *
     * @return 如果有正在运行当中的任务，return true；没有则return false
     */
    fun isRunning(): Boolean = this.currentTaskName != null

    /**
     * 给定taskName，启动秒表
     *
     * @param name taskName
     * @throws IllegalStateException 如果当前秒表当中还有任务没有结束
     */
    fun start(name: String) {
        if (this.currentTaskName != null) {
            throw IllegalStateException("当前秒表的任务[${this.currentTaskName}]正在运行当中，不能重复启动秒表")
        }
        this.startTimeNanos = System.nanoTime()
        this.currentTaskName = name
    }

    /**
     * 停止当前task，开始去记录当前任务的相关信息
     *
     * @throws IllegalStateException 如果当前秒表之前没有启动过，就想要停止
     */
    fun stop() {
        val currentTaskName = this.currentTaskName ?: throw IllegalStateException("当前秒表还没启动，请先启动")
        val lastTimeNanos = System.nanoTime() - startTimeNanos

        // 构建lastInfoList
        val lastTaskInfo = TaskInfo(currentTaskName, lastTimeNanos)
        this.taskInfoList += lastTaskInfo
        this.taskCount++
        this.lastTaskInfo = lastTaskInfo

        // 把秒包总的运行时间，加上当前的运行时间
        this.totalTimeNanos += lastTimeNanos

        // 重设 currentTaskName
        this.currentTaskName = null
    }

    /**
     * 记录秒表当中的一个任务的信息
     *
     * @param name taskName
     * @param timeNanos 该任务的运行时间
     */
    class TaskInfo(val name: String, val timeNanos: Long)
}