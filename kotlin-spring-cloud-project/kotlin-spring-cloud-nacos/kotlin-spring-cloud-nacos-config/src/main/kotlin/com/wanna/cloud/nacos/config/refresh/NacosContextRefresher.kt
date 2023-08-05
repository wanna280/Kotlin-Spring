package com.wanna.cloud.nacos.config.refresh

import com.alibaba.nacos.api.config.listener.AbstractSharedListener
import com.wanna.boot.context.event.ApplicationReadyEvent
import com.wanna.cloud.endpoint.event.RefreshEvent
import com.wanna.cloud.nacos.config.NacosConfigManager
import com.wanna.cloud.nacos.config.NacosPropertySource
import com.wanna.cloud.nacos.config.NacosPropertySourceRepository
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.event.ApplicationListener

/**
 * Nacos的ContextRefresher, 负责给NacosConfigManager的ConfigService当中去注册监听器;
 *
 * 当配置文件发生改变时, 会自动发布RefreshEvent事件, 该事件它会被RefreshEventListener所处理,
 * 因此会去触发SpringCloudContext当中的ContextRefresher, 从而去刷新Environment, 以及RefreshScope内的对象
 *
 * @see NacosConfigManager
 *
 * @param nacosConfigManager Nacos的ConfigManager, 提供对于原生Nacos的ConfigService的相关操作
 */
open class NacosContextRefresher(private val nacosConfigManager: NacosConfigManager) :
    ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

    /**
     * ApplicationContext
     */
    private lateinit var applicationContext: ApplicationContext

    /**
     * 设置ApplicationContext
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 当SpringApplication已经就绪时, 将所有的[NacosPropertySource]去注册监听器;
     * 在NacosServer的配置文件更新时, 可以通知所有的RefreshScope内的Bean去完成Rebind重新绑定
     *
     * @param event ApplicationReadyEvent(SpringApplication已经准备好的事件)
     */
    override fun onApplicationEvent(event: ApplicationReadyEvent) {

        // 获取ConfigService
        val configService = nacosConfigManager.getConfigService()

        // 对所有的NacosPropertySource去添加监听器, 当该配置文件发生变化时, 可以自动触发RefreshEvent
        NacosPropertySourceRepository.getAll().forEach {

            // 使用ConfigService, 为涉及到的每个NacosServer的配置文件去添加Listener
            configService.addListener(it.dataId, it.group, object : AbstractSharedListener() {
                override fun innerReceive(dataId: String?, group: String?, configInfo: String?) =
                    applicationContext.publishEvent(RefreshEvent(this@NacosContextRefresher))
            })
        }
    }
}