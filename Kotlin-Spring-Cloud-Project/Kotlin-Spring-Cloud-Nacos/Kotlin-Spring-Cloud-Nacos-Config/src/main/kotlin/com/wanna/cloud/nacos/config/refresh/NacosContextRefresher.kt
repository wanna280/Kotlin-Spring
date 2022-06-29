package com.wanna.cloud.nacos.config.refresh

import com.alibaba.nacos.api.config.listener.AbstractSharedListener
import com.wanna.boot.context.event.ApplicationReadyEvent
import com.wanna.cloud.context.endpoint.event.RefreshEvent
import com.wanna.cloud.nacos.config.NacosConfigManager
import com.wanna.cloud.nacos.config.NacosPropertySourceRepository
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.event.ApplicationListener

/**
 * Nacos的ContextRefresher，负责给NacosConfigManager的ConfigService当中去注册监听器；
 *
 * 当配置文件发生改变时，会自动发布RefreshEvent事件，该事件它会被RefreshEventListener所处理，
 * 因此会去触发SpringCloudContext当中的ContextRefresher，从而去刷新Environment，以及RefreshScope内的对象
 *
 * @see NacosConfigManager
 */
open class NacosContextRefresher(private val nacosConfigManager: NacosConfigManager) :
    ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 当SpringApplication已经就绪时，将所有的NacosPropertySource去注册监听器；
     * 方便在配置文件更新时，可以通知所有的RefreshScope内的Bean去完成Rebind
     *
     * @param event ApplicationReadyEvent
     */
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val configService = nacosConfigManager.getConfigService()
        NacosPropertySourceRepository.getAll().forEach {
            configService.addListener(it.dataId, it.group, object : AbstractSharedListener() {
                override fun innerReceive(dataId: String?, group: String?, configInfo: String?) {
                    this@NacosContextRefresher.applicationContext.publishEvent(RefreshEvent(this@NacosContextRefresher))
                }
            })
        }
    }
}