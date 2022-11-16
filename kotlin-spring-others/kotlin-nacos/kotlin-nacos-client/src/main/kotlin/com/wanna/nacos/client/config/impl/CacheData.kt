package com.wanna.nacos.client.config.impl

import com.wanna.nacos.api.config.listener.Listener
import java.util.concurrent.CopyOnWriteArrayList

/**
 * CacheData, 描述的是NacosConfig的一个配置文件, 负责维护一个ConfigClient的配置文件相关的监听器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 *
 * @param dataId dataId
 * @param group group
 * @param tenant tenant(namespace)
 */
class CacheData(val dataId: String = "", val group: String = "", val tenant: String = "") {
    /**
     * 当前文件内容的Md5值
     */
    @Volatile
    var md5: String = ""
        private set

    /**
     * 当前配置文件的内容
     */
    @Volatile
    var content: String = ""
        set(value) {
            // 当更新content内容时, 同步更新MD5
            field = value
            md5 = value
        }

    /**
     * fileType
     */
    var fileType: String = ""

    /**
     * taskId
     */
    var taskId: Int = 0


    /**
     * 维护所有的Listener
     */
    private val listeners = CopyOnWriteArrayList<ManagerListenerWrap>()

    /**
     * 添加Listener
     *
     * @param listener listener
     */
    fun addListener(listener: Listener) {
        this.listeners.addIfAbsent(ManagerListenerWrap(listener))
    }

    /**
     * 移除Listener
     *
     * @param listener Listener
     */
    fun removeListener(listener: Listener) {
        this.listeners.remove(ManagerListenerWrap(listener))
    }

    /**
     * 获取当前CacheData当中的所有Listener列表
     *
     * @return List of Listener
     */
    fun getListeners(): List<Listener> = this.listeners.map { it.listener }

    /**
     * 检查Md5是否发生变化, 如果Md5已经发生变化了, 那么需要通知CacheData本地的所有Listener
     *
     * @see listeners
     */
    fun checkListenerMd5() {
        listeners.forEach { listenerWrap ->
            // 如果这一时刻, 该文件的MD5发生变化了的话, 需要Callback所有的Listener
            if (listenerWrap.lastCallMd5 != md5) {
                safeNotifyListener(dataId, group, tenant, md5, listenerWrap)
            }
        }
    }


    /**
     * 通知所有的Listener
     *
     * @param dataId dataId
     * @param group group
     * @param tenant tenant(namespace)
     * @param md5 md5
     * @param listenerWrap Listener
     */
    private fun safeNotifyListener(
        dataId: String,
        group: String,
        tenant: String,
        md5: String,
        listenerWrap: ManagerListenerWrap
    ) {

        val listener = listenerWrap.listener
        val executor = listener.getExecutor()

        val job = Runnable {
            // callbackListener, 去接收配置文件的变更去进行处理...
            listener.receiveConfigInfo(content)

            // 变更Md5和content
            listenerWrap.lastCallMd5 = md5
            listenerWrap.lastContent = content
        }
        if (executor != null) {
            executor.execute(job)
        } else {
            job.run()
        }
    }


    /**
     * 对于一个Listener的包装
     *
     * @param listener listener
     * @param lastContent 上一次的文件内容(如果文件内容发生变化, 会很及时发生变更)
     * @param lastCallMd5 上次调用时的Md5值(如果文件内容发生变化, 会很及时发生变更)
     */
    private class ManagerListenerWrap(
        val listener: Listener,
        var lastCallMd5: String? = null,
        var lastContent: String? = null
    )
}