package com.wanna.nacos.client.config.impl

import com.wanna.nacos.api.config.ConfigChangeEvent
import com.wanna.nacos.api.config.listener.AbstractListener

/**
 * 处理属性值发生变化的Listener, 比如配置文件当中的"a=1"变成"a=2"的这种情况
 * 我们就需要去进行Diff, 并得到配置文件的两个版本之间的区别
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
abstract class AbstractConfigChangeListener : AbstractListener() {

    /**
     * 接收配置文件当中的属性值发生变化的事件, 并进行处理
     *
     * @param event 配置文件发生变化的属性值列表的事件
     */
    abstract fun receiveConfigChange(event: ConfigChangeEvent)

    /**
     * 为接收配置文件发生变更的方法去提供空实现, 这样子类就不需要去进行继续实现;
     * 推荐子类使用[receiveConfigChange]去接收配置文件的变更
     *
     * @param configInfo 当前的配置文件的内容
     */
    override fun receiveConfigInfo(configInfo: String) {

    }
}