package com.wanna.nacos.config.server.service.repository

import com.wanna.nacos.config.server.model.ConfigInfo
import com.wanna.nacos.config.server.model.ConfigInfoWrapper

/**
 * 真正地去执行持久操作的Service, 比如支持去使用SQL去将配置文件去保存到DB当中
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
interface PersistService {

    /**
     * 列举出来所有的GroupKey的MD5
     *
     * @return 所有的GroupKey的MD5值
     */
    fun listAllGroupKeyMd5(): List<ConfigInfoWrapper>

    /**
     * 列举出来给定的时间取件内的所有的发生变更的信息
     *
     * @param startTime startTime
     * @param endTime endTime
     * @return 该时间区间内发生变更的ConfigInfo
     */
    fun findChangeConfig(startTime: Long, endTime: Long): List<ConfigInfoWrapper>

    /**
     * 根据dataId&group&tenant去删除一个配置文件ConfigInfo
     *
     * @param dataId dataId
     * @param group group
     * @param srcIp srcIp
     * @param srcUser srcUser
     */
    fun removeConfigInfo(dataId: String, group: String, tenant: String, srcIp: String, srcUser: String)

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