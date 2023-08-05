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

    /**
     * 真正地去解析一个配置文件的变更
     *
     * @param oldContent 该配置文件当中的原始的内容
     * @param newContent 该配置文件当中的新的内容
     * @param type fileType
     * @return 解析得到的新旧配置文件发生变更的情况
     */
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