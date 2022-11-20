package com.wanna.nacos.api.config

/**
 * 描述配置文件发生变更时的其中一个属性值的变化情况
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 *
 * @param key propertyKey
 * @param oldValue 旧的值
 * @param newValue 新的值
 */
class ConfigChangeItem(var key: String, var oldValue: String?, var newValue: String?) {
    var propertyChangeType: PropertyChangeType? = null
    override fun toString(): String {
        return "ConfigChangeItem(key='$key', oldValue=$oldValue, newValue=$newValue, propertyChangeType=$propertyChangeType)"
    }
}