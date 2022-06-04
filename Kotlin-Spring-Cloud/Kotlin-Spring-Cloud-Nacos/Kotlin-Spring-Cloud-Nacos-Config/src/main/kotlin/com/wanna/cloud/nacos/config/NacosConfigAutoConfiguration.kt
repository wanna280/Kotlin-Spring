package com.wanna.cloud.nacos.config

import com.wanna.cloud.nacos.config.refresh.NacosContextRefresher
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
open class NacosConfigAutoConfiguration {

    /**
     * 获取配置文件当中对于Nacos的配置信息
     */
    @Bean
    open fun nacosConfigProperties(): NacosConfigProperties {
        return NacosConfigProperties()
    }

    /**
     * NacosConfigManager，负责去进行配置文件的获取
     */
    @Bean
    open fun nacosConfigManager(nacosConfigProperties: NacosConfigProperties): NacosConfigManager {
        return NacosConfigManager(nacosConfigProperties)
    }

    /**
     * Nacos的ContextRefresher，给NacosClient去注册监听器
     */
    @Bean
    open fun nacosContextRefresher(nacosConfigManager: NacosConfigManager): NacosContextRefresher {
        return NacosContextRefresher(nacosConfigManager)
    }
}