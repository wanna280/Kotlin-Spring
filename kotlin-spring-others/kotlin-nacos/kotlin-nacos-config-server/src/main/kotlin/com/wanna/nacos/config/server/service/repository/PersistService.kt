package com.wanna.nacos.config.server.service.repository

import com.wanna.nacos.config.server.model.ConfigInfo

/**
 * 真正地去执行持久操作的Service, 比如支持去使用SQL去将配置文件去保存到DB当中
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
interface PersistService {

    /**
     * 根据dataId&group&tenant去加载到对应的配置文件信息
     *
     * @param dataId dataId
     * @param group group
     * @param tenant tenant(namespace)
     * @return 加载到的配置文件的ConfigInfo
     */
    fun findConfigInfo(dataId: String, group: String, tenant: String): ConfigInfo?

    /**
     * 插入/更新一条ConfigInfo配置文件信息
     *
     * @param srcIp srcIp
     * @param srcUser srcUser
     * @param configInfo ConfigInfo
     * @param time 时间戳
     * @param advanceConfigInfo 高级配置信息
     * @param notify notify?
     */
    fun insertOrUpdate(
        srcIp: String,
        srcUser: String,
        configInfo: ConfigInfo,
        time: Long,
        advanceConfigInfo: Map<String, Any>,
        notify: Boolean
    )
}