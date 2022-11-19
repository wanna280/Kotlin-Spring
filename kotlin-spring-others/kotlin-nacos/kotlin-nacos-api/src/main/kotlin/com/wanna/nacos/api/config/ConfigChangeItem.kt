package com.wanna.nacos.api.config

/**
 * 一个配置文件改变的元素
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
class ConfigChangeItem(var key: String, var oldValue: String?, var newValue: String?) {
    var propertyChangeType: PropertyChangeType? = null
}