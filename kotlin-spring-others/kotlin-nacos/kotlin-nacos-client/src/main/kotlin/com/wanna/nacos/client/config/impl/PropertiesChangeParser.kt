package com.wanna.nacos.client.config.impl

import com.wanna.nacos.api.config.ConfigChangeItem
import java.io.StringReader
import java.util.*

/**
 * Properties配置文件变更的解析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
class PropertiesChangeParser : AbstractConfigChangeParser("properties") {
    @Suppress("UNCHECKED_CAST")
    override fun doParse(oldContent: String?, newContent: String?, type: String): Map<String, ConfigChangeItem> {
        val oldProperties = Properties()
        val newProperties = Properties()
        if (oldContent != null && oldContent.isNotBlank()) {
            oldProperties.load(StringReader(oldContent))
        }
        if (newContent != null && newContent.isNotBlank()) {
            newProperties.load(StringReader(newContent))
        }
        return filterChangeData(oldProperties as Map<String, Any>, newProperties as Map<String, Any>)
    }
}