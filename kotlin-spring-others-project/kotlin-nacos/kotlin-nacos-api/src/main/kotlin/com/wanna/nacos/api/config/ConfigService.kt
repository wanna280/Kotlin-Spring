package com.wanna.nacos.api.config

import com.wanna.nacos.api.config.listener.Listener
import com.wanna.nacos.api.exception.NacosException
import kotlin.jvm.Throws

/**
 * ConfigService
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
interface ConfigService {

    /**
     * 获取dataId和group对应的配置文件
     *
     * @param dataId dataId
     * @param group group
     * @param timeoutMs 拉取配置文件的超时时间(单位为ms)
     * @return 加载到的配置文件
     */
    fun getConfig(dataId: String, group: String, timeoutMs: Long): String

    /**
     * 添加一个Listener, 监听dataId和group对应的配置文件的变更
     *
     * @param dataId dataId
     * @param group group
     * @param listener 需要添加的Listener
     */
    fun addListener(dataId: String, group: String, listener: Listener)

    /**
     * 移除一个监听dataId和group对应的配置文件的Listener
     *
     * @param dataId dataId
     * @param group group
     * @param listener 需要去进行移除的Listener
     */
    fun removeListener(dataId: String, group: String, listener: Listener)

    /**
     * 使用文本(fileType=txt)的方式去发布一个配置文件
     *
     * @param dataId dataId
     * @param group group
     * @param content 配置文件的内容
     */
    fun publishConfig(dataId: String, group: String, content: String): Boolean

    /**
     * 以给定具体文件类型(fileType)的方式去发布一个配置文件
     *
     * @param dataId dataId
     * @param group group
     * @param content 配置文件的内容
     * @param fileType 文件类型(比如TEXT/PROPERTIES/JSON等)
     */
    fun publishConfig(dataId: String, group: String, content: String, fileType: String): Boolean

    /**
     * 根据dataId和group去删除一个配置文件
     *
     * @param dataId dataId
     * @param group group
     * @return 删除文件是否成功?
     */
    fun removeConfig(dataId: String, group: String): Boolean

    /**
     * 关闭当前ConfigService
     */
    @Throws(NacosException::class)
    fun shutdown()
}