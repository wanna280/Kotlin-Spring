package com.wanna.nacos.config.server.service.dump

import com.wanna.nacos.config.server.manager.TaskManager
import com.wanna.nacos.config.server.service.dump.processor.DumpAllProcessor
import com.wanna.nacos.config.server.service.dump.processor.DumpChangeProcessor
import com.wanna.nacos.config.server.service.dump.task.DumpChangeTask
import com.wanna.nacos.config.server.service.dump.task.DumpTask
import com.wanna.nacos.config.server.service.repository.PersistService
import com.wanna.nacos.config.server.utils.ConfigExecutor
import com.wanna.nacos.config.server.utils.GroupKey2
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

/**
 * 用于去进行Dump转存的Service
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
abstract class DumpService {

    private var persistService: PersistService? = null

    private val taskManager = TaskManager("com.wanna.nacos.server.DumpTaskManager")


    /**
     * 完成Service的初始化工作
     */
    @Throws(Throwable::class)
    protected abstract fun init()

    open fun setPersistService(persistService: PersistService) {
        this.persistService = persistService
    }

    open fun getPersistService(): PersistService =
        persistService ?: throw IllegalStateException("PersistService不能为null")

    open fun dump(dataId: String, group: String, tenant: String, tag: String, lastModified: Long, handleIp: String) {
        val groupKey = GroupKey2.getKeyTenant(dataId, group, tenant)
        val taskKey = StringJoiner("+").add(dataId).add(group).add(tenant).add(tag).toString()

        // add Task
        taskManager.addTask(taskKey, DumpTask(groupKey, lastModified, handleIp, tag))
    }

    protected open fun dumpOperate(dumpAllProcessor: DumpAllProcessor) {
        dumpConfigInfo(dumpAllProcessor)
    }

    /**
     * dump ConfigInfo
     *
     * @param dumpAllProcessor DumpAllProcessor
     */
    private fun dumpConfigInfo(dumpAllProcessor: DumpAllProcessor) {

        // 构建出来一个DumpChangeProcessor
        // 它会从PersistService当中拉取到所有的配置文件, 并更新到ConfigCacheService当中
        val dumpChangeProcessor = DumpChangeProcessor(this)
        dumpChangeProcessor.process(DumpChangeTask(this))

        val checkMd5Task = Runnable {

        }

        ConfigExecutor.scheduleConfigTask(checkMd5Task, 0, 12, TimeUnit.HOURS)
    }
}