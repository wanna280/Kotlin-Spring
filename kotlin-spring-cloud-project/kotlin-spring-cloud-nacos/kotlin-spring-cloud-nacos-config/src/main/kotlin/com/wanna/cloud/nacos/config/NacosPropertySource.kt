package com.wanna.cloud.nacos.config

import com.wanna.framework.core.environment.PropertiesPropertySource
import java.util.Properties

/**
 * 这是一个Nacos的PropertiesSource，负责维护Nacos配置中心当中加载过来的数据情况
 *
 * @param dataId nacosConfig的dataId
 * @param group nacosConfig的group
 * @param properties 加载到的配置文件情况
 */
open class NacosPropertySource(val dataId: String, val group: String, properties: Properties) :
    PropertiesPropertySource("$group.$dataId", properties)