package com.wanna.cloud.nacos.config

import com.wanna.cloud.bootstrap.config.PropertySourceLocator
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * Nacos对于Bootstrap容器当中的配置
 */
@Configuration(proxyBeanMethods = false)
open class NacosBootstrapConfiguration {

    /**
     * SpringCloudContext提供的PropertySourceLocator扩展接口，负责去加载远程ConfigServer当中的配置文件
     *
     * @param nacosConfigManager NacosConfigManager
     * @return Nacos的PropertySourceLocator
     */
    @Bean
    open fun nacosPropertySourceLocator(nacosConfigManager: NacosConfigManager): PropertySourceLocator {
        return NacosPropertySourceLocator(nacosConfigManager)
    }

    @Bean
    open fun nacosConfigManager(nacosConfigProperties: NacosConfigProperties): NacosConfigManager {
        return NacosConfigManager(nacosConfigProperties)
    }

    /**
     * 给Bootstrap容器当中导入一个NacosProperties去获取到Nacos的配置信息
     *
     * @return NacosConfigProperties
     */
    @Bean
    open fun nacosConfigProperties(): NacosConfigProperties {
        return NacosConfigProperties()
    }
}