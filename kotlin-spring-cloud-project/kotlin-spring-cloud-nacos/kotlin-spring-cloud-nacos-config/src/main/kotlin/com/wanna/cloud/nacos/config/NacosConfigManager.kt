package com.wanna.cloud.nacos.config

import com.alibaba.nacos.api.NacosFactory
import com.alibaba.nacos.api.config.ConfigService
import com.wanna.cloud.nacos.config.NacosConfigProperties.Companion.DEFAULT_NACOS_GROUP
import com.wanna.cloud.nacos.config.NacosConfigProperties.Companion.DEFAULT_TIMEOUT
import com.wanna.framework.util.StringUtils
import java.io.StringReader
import java.util.*

/**
 * 这是一个NacosConfigManager, 它内部集成了Nacos的ConfigService, 去完成Nacos的配置中心的配置文件的拉取
 *
 * @param nacosConfigProperties Nacos的属性配置信息
 *
 * @see NacosFactory
 * @see ConfigService
 */
open class NacosConfigManager(val nacosConfigProperties: NacosConfigProperties) {

    /**
     * 来自于原生Nacos API的ConfigService, 提供对于NacosServer当中的配置文件的加载
     */
    private var configService: ConfigService? = null

    /**
     * 获取Nacos的ConfigService, 如果还没初始化的话, 先完成初始化再返回
     *
     * @return ConfigService
     */
    open fun getConfigService(): ConfigService {
        initConfigServiceIfNecessary()
        return this.configService!!
    }

    /**
     * 根据dataId去获取该配置文件的属性信息(对于group, 则采用默认的group)
     *
     * @param dataId dataId
     * @return 配置文件当中得到的属性信息Properties
     */
    open fun getConfig(dataId: String): Properties = getConfig(dataId, DEFAULT_NACOS_GROUP)

    /**
     * 根据dataId和group, 去加载到配置文件当的属性信息
     *
     * @param group group
     * @param dataId dataId
     * @return 配置文件当中得到的属性信息Properties
     */
    open fun getConfig(dataId: String, group: String): Properties = getConfig(dataId, group, DEFAULT_TIMEOUT)

    /**
     * 根据dataId和group, 去加载到配置文件当的属性信息
     *
     * @param group group
     * @param dataId dataId
     * @param timeout timeout超时时间
     * @return 配置文件当中得到的属性信息Properties
     */
    open fun getConfig(dataId: String, group: String, timeout: Long): Properties {
        val configService = getConfigService()
        val properties = Properties()
        // 通过ConfigService去加载配置中心的配置文件...
        val config = configService.getConfig(dataId, group, timeout)

        // 将ConfigService当中加载到的配置文件去转换到Properties对象当中去
        if (StringUtils.hasText(config)) {
            properties.load(StringReader(config))
        }
        return properties
    }

    /**
     * 如果必要的话, 先去初始化Nacos的ConfigService
     *
     * @see NacosFactory
     * @see ConfigService
     */
    private fun initConfigServiceIfNecessary() {
        var configService = this.configService
        if (configService == null) {
            configService = NacosFactory.createConfigService(nacosConfigProperties.getNacosConfigProperties())
            this.configService = configService
        }
    }
}