package com.wanna.cloud.context.endpoint.event

import com.wanna.boot.context.event.ApplicationReadyEvent
import com.wanna.cloud.context.refresh.ContextRefresher
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.SmartApplicationListener
import com.wanna.framework.core.util.ClassUtils
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 它是一个监听RefreshEvent的Listener，当RefreshEvent到来时，会自动触发ContextRefresher去完成refresh；
 *
 * 当Context发生变化时，需要发布一个Refresh事件-->RefreshEventListener处理-->ContextRefresher.refresh-->
 * 更新配置文件到环境-->发布EnvironmentChangeEvent事件--->RefreshScope.refreshAll刷新RefreshScope内的全部Bean
 *
 * @see ContextRefresher
 */
open class RefreshEventListener(private val contextRefresher: ContextRefresher) : SmartApplicationListener {

    private val ready = AtomicBoolean(false)

    override fun onApplicationEvent(event: ApplicationEvent) {
        if (event is RefreshEvent) {
            handleRefreshEvent(event)
        }
        if (event is ApplicationReadyEvent) {
            handleApplicationEvent(event)
        }
    }

    override fun supportEventType(eventType: Class<out ApplicationEvent>): Boolean {
        return ClassUtils.isAssignFrom(ApplicationReadyEvent::class.java, eventType) ||
                ClassUtils.isAssignFrom(RefreshEvent::class.java, eventType)
    }

    open fun handleApplicationEvent(event: ApplicationReadyEvent) {
        this.ready.compareAndSet(false, true)
    }

    open fun handleRefreshEvent(event: RefreshEvent) {
        if (this.ready.get()) {
            val keys = contextRefresher.refresh()
        }
    }
}