package com.wanna.cloud.nacos.config

import com.wanna.framework.core.environment.PropertiesPropertySource
import com.wanna.framework.core.environment.PropertySource
import java.util.Properties

/**
 * 这是一个Nacos的[PropertySource], 继承[PropertiesPropertySource]去负责维护Nacos配置中心当中加载过来配置文件
 *
 * @param dataId nacosConfig的dataId
 * @param group nacosConfig的group
 * @param properties 配置文件当中的属性信息
 */
open class NacosPropertySource(val dataId: String, val group: String, properties: Properties) :
    PropertiesPropertySource("$group.$dataId", properties)