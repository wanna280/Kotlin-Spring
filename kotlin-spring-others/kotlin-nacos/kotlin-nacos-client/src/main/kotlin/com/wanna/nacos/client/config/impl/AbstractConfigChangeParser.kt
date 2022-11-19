package com.wanna.nacos.client.config.impl

import com.wanna.nacos.api.config.ConfigChangeItem
import com.wanna.nacos.api.config.PropertyChangeType
import com.wanna.nacos.api.config.listener.ConfigChangeParser

/**
 * 抽象的配置文件变更的解析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 *
 * @param configType 支持去进行处理的配置文件类型
 */
abstract class AbstractConfigChangeParser(private val configType: String) : ConfigChangeParser {

    /**
     * 如果给定的type和configType匹配的话, 那么就支持去进行处理
     *
     * @param type 发生变更的文件的fileType
     * @return 如果和configType和type匹配的话, return true; 否则return false
     */
    override fun isResponsibleFor(type: String): Boolean = configType.equals(type, false)

    /**
     * 根据新旧Map去Diff出来变更的情况
     *
     * @param oldMap oldMap
     * @param newMap newMap
     * @return oldMap和newMap去进行Diff的变更情况
     */
    protected open fun filterChangeData(
        oldMap: Map<String, Any>,
        newMap: Map<String, Any>
    ): Map<String, ConfigChangeItem> {
        val result = LinkedHashMap<String, ConfigChangeItem>()
        oldMap.forEach { (key, value) ->
            val configChangeItem: ConfigChangeItem
            // 如果旧的元素在newMap当中也有的话...
            if (newMap.containsKey(key)) {

                // 如果值相等, 那么不算是变更, pass
                if (newMap[key] == value) {
                    return@forEach
                }
                // 如果值不等, 那么就算是发生变更了...(MODIFIED)
                configChangeItem = ConfigChangeItem(key, oldMap[key].toString(), newMap[key].toString())
                configChangeItem.propertyChangeType = PropertyChangeType.MODIFIED
                // 如果在newMap当中不存在的话, 说明是被删除掉了...
            } else {
                configChangeItem = ConfigChangeItem(key, oldMap[key].toString(), null)
                configChangeItem.propertyChangeType = PropertyChangeType.DELETED
            }

            // add to result
            result[key] = configChangeItem
        }

        newMap.forEach { (key, value) ->
            // 这里只需要处理oldMap当中不存在的那些(ADDED)...因为存在的情况, 在前面已经处理过了..
            if (!oldMap.containsKey(key)) {
                val configChangeItem = ConfigChangeItem(key, null, value.toString())
                configChangeItem.propertyChangeType = PropertyChangeType.ADDED
                result[key] = configChangeItem
            }
        }
        return result
    }
}