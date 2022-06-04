package com.wanna.cloud.nacos.config

import com.alibaba.nacos.api.NacosFactory
import com.alibaba.nacos.api.config.ConfigService
import com.alibaba.nacos.client.config.NacosConfigService
import com.wanna.cloud.nacos.config.NacosConfigProperties.Companion.DEFAULT_NACOS_GROUP
import com.wanna.cloud.nacos.config.NacosConfigProperties.Companion.DEFAULT_TIMEOUT
import com.wanna.framework.core.util.StringUtils
import java.io.StringReader
import java.util.Properties

/**
 * 这是一个NacosConfigManager，它内部集成了Nacos的ConfigService，去完成Nacos的配置中心的配置文件的拉取
 */
open class NacosConfigManager(val nacosConfigProperties: NacosConfigProperties) {

    // 来自于Nacos的ConfigService
    private var configService: ConfigService? = null

    open fun getConfigService(): ConfigService {
        initConfigServiceIfNecessary()
        return this.configService!!
    }

    open fun getConfig(dataId: String, group: String): Properties {
        return getConfig(dataId, group, DEFAULT_TIMEOUT)
    }

    open fun getConfig(dataId: String): Properties {
        return getConfig(dataId, DEFAULT_NACOS_GROUP, DEFAULT_TIMEOUT)
    }

    open fun getConfig(dataId: String, group: String, timeout: Long): Properties {
        val configService = getConfigService()
        val properties = Properties()
        // 通过ConfigService去加载配置中心的配置文件...
        val config = configService.getConfig(dataId, group, timeout)
        if (StringUtils.hasText(config)) {
            properties.load(StringReader(config))
        }
        return properties
    }

    private fun initConfigServiceIfNecessary() {
        var configService = this.configService
        if (configService == null) {
            configService = NacosFactory.createConfigService(nacosConfigProperties.getNacosConfigProperties())
            this.configService = configService
        }
    }
}