package com.wanna.nacos.config.server.service.dump

import com.wanna.nacos.config.server.manager.TaskManager
import com.wanna.nacos.config.server.service.dump.processor.DumpAllProcessor
import com.wanna.nacos.config.server.service.dump.processor.DumpChangeProcessor
import com.wanna.nacos.config.server.service.dump.processor.DumpProcessor
import com.wanna.nacos.config.server.service.dump.task.DumpAllTask
import com.wanna.nacos.config.server.service.dump.task.DumpChangeTask
import com.wanna.nacos.config.server.service.dump.task.DumpTask
import com.wanna.nacos.config.server.service.repository.PersistService
import com.wanna.nacos.config.server.utils.ConfigExecutor
import com.wanna.nacos.config.server.utils.GroupKey2
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 用于去进行Dump转存的Service
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 *
 * @param persistService PersistService
 */
abstract class DumpService(val persistService: PersistService) {

    /**
     * TaskManager
     */
    private val taskManager = TaskManager("com.wanna.nacos.server.DumpTaskManager")

    init {
        // 设置默认的TaskProcessor为DumpProcessor, 专门用来处理DumpTask任务
        taskManager.defaultTaskProcessor = DumpProcessor(this)

        // 添加一个DumpAll的Processor
        taskManager.addProcessor(DumpAllTask.TASK_ID, DumpAllProcessor(this))
    }


    /**
     * 完成DumpService的初始化工作
     */
    @Throws(Throwable::class)
    protected abstract fun init()

    /**
     * 往[TaskManager]当中去添加一个[DumpTask]任务, 执行一次dump操作,
     * 等待[TaskManager]当中的异步任务的调度和执行, 从而去实现通知客户端配置文件发生变化
     *
     * @param dataId dataId
     * @param group group
     * @param tenant tenant
     * @param tag tag
     * @param lastModified lastModified
     * @param handleIp handleIp
     */
    open fun dump(dataId: String, group: String, tenant: String, tag: String, lastModified: Long, handleIp: String) {
        val groupKey = GroupKey2.getKeyTenant(dataId, group, tenant)

        // TaskKey
        val taskKey = StringJoiner("+").add(dataId).add(group).add(tenant).add(tag).toString()

        // add DumpTask, 等待TaskManager去异步执行DumpTaskProcessor
        // @see com.wanna.nacos.config.server.service.dump.processor.DumpProcessor
        taskManager.addTask(taskKey, DumpTask(groupKey, lastModified, handleIp, tag))
    }

    /**
     * 执行dump操作
     *
     * @param dumpAllProcessor DumpAllProcessor
     */
    protected open fun dumpOperate(dumpAllProcessor: DumpAllProcessor) {
        dumpConfigInfo(dumpAllProcessor)
    }

    /**
     * dump ConfigInfo
     *
     * @param dumpAllProcessor DumpAllProcessor
     */
    private fun dumpConfigInfo(dumpAllProcessor: DumpAllProcessor) {
        var dumpAll = true

        // 根据是否dumpAll, 走不同的逻辑
        if (dumpAll) {
            dumpAllProcessor.process(DumpAllTask())
        } else {
            // 构建出来一个DumpChangeProcessor
            // 它会从PersistService当中拉取到所有的配置文件, 并更新到ConfigCacheService当中
            val dumpChangeProcessor = DumpChangeProcessor(this)
            dumpChangeProcessor.process(DumpChangeTask())
        }

        val checkMd5Task = Runnable {

        }

        ConfigExecutor.scheduleConfigTask(checkMd5Task, 0, 12, TimeUnit.HOURS)
    }
}