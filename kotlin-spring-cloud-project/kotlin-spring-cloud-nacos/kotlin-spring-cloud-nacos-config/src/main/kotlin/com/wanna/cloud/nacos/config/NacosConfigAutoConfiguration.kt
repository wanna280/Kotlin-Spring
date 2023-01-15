package com.wanna.cloud.nacos.config

import com.wanna.cloud.nacos.config.refresh.NacosContextRefresher
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * Nacos的自动配置类
 */
@Configuration(proxyBeanMethods = false)
open class NacosConfigAutoConfiguration {

    /**
     * 获取配置文件当中对于Nacos的配置信息
     *
     * @return NacosConfigProperties
     */
    @Bean
    open fun nacosConfigProperties(): NacosConfigProperties {
        return NacosConfigProperties()
    }

    /**
     * NacosConfigManager, 负责去进行配置文件的获取
     *
     * @param nacosConfigProperties Nacos的配置信息
     * @return NacosConfigManager, 提供对于NacosServer的配置文件的加载功能
     */
    @Bean
    open fun nacosConfigManager(nacosConfigProperties: NacosConfigProperties): NacosConfigManager {
        return NacosConfigManager(nacosConfigProperties)
    }

    /**
     * Nacos的ContextRefresher, 给NacosClient去注册监听器
     *
     * @param nacosConfigManager NacosConfigManager
     * @return NacosContextRefresher(为所有的NacosPropertySource对应的文件去添加监听器, 当对应的文件发生变更时, 自动发布RefreshEvent事件)
     */
    @Bean
    open fun nacosContextRefresher(nacosConfigManager: NacosConfigManager): NacosContextRefresher {
        return NacosContextRefresher(nacosConfigManager)
    }
}