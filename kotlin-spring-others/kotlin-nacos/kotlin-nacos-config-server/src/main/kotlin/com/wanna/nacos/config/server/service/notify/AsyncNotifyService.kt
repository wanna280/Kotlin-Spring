package com.wanna.nacos.config.server.service.notify

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.stereotype.Service
import com.wanna.nacos.api.notify.Event
import com.wanna.nacos.api.notify.NotifyCenter
import com.wanna.nacos.api.notify.listener.Subscriber
import com.wanna.nacos.config.server.controller.CommunicationController
import com.wanna.nacos.config.server.model.event.ConfigDataChangeEvent
import com.wanna.nacos.config.server.service.dump.DumpService
import com.wanna.nacos.config.server.utils.ConfigExecutor
import java.util.*
import javax.annotation.PostConstruct

/**
 * 执行异步的通知配置文件发生变化的任务的Service
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
@Service
class AsyncNotifyService {

    /**
     * 自动注入DumpService
     */
    @Autowired
    private lateinit var dumpService: DumpService


    @PostConstruct
    fun init() {
        // 注册一个Listener, 监听配置文件发生变化的事件...(ConfigDataChangeEvent)
        NotifyCenter.registerSubscriber(object : Subscriber<ConfigDataChangeEvent>() {
            override fun onEvent(event: ConfigDataChangeEvent) {
                val queue = LinkedList<NotifySingleTask>()
                queue.add(NotifySingleTask(event.dataId, event.group, event.tenant, event.lastModifiedTs))
                ConfigExecutor.executeAsyncNotify(AsyncTask(queue))
            }

            override fun subscribeType(): Class<out Event> = ConfigDataChangeEvent::class.java
        })
    }

    class NotifySingleTask(dataId: String, group: String, tenant: String, lastModifiedTs: Long) :
        NotifyTask(dataId, group, tenant, lastModifiedTs) {

    }

    /**
     * 去进行通知异步配置文件发生变更的任务
     *
     * @param queue 需要去进行通知的任务的列表
     */
    inner class AsyncTask(private val queue: Queue<NotifySingleTask>) : Runnable {
        override fun run() {
            queue.forEach {
                dumpService.dump(it.dataId, it.group, it.tenant, "", it.lastModified, "")
            }
        }
    }
}