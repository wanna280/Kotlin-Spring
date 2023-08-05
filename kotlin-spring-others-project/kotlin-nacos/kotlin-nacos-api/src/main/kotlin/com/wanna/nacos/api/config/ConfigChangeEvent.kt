package com.wanna.nacos.api.config

/**
 * 描述一个配置文件当中的一些发生变化的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 *
 * @param changedItem 配置文件当中发生变化的属性
 */
class ConfigChangeEvent(private val changedItem: Map<String, ConfigChangeItem>) {

    /**
     * 根据Key去获取一个发生改变的属性值
     *
     * @param key 需要去进行获取的属性值的Key
     */
    fun getChangedItem(key: String): ConfigChangeItem? = changedItem[key]

    /**
     * 获取所有的发生改变的属性值列表
     *
     * @return 所有发生改变的属性值列表
     */
    fun getChangedItems(): Collection<ConfigChangeItem> = changedItem.values

}