package com.wanna.cloud.nacos.config

import com.wanna.framework.core.environment.PropertiesPropertySource
import java.util.Properties

/**
 * 这是一个Nacos的PropertiesSource
 */
open class NacosPropertySource(val dataId: String, val group: String, properties: Properties) :
    PropertiesPropertySource("$group.$dataId", properties)