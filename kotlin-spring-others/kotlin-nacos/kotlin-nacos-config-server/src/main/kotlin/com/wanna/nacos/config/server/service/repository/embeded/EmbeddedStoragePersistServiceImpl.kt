package com.wanna.nacos.config.server.service.repository.embeded

import com.wanna.framework.context.stereotype.Component
import com.wanna.nacos.config.server.model.ConfigInfo
import com.wanna.nacos.config.server.service.repository.PersistService
import com.wanna.nacos.config.server.utils.GroupKey2.getKeyTenant
import java.util.concurrent.ConcurrentHashMap

/**
 * 嵌入式存储的[PersistService]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/14
 *
 * @see PersistService
 */
@Component
open class EmbeddedStoragePersistServiceImpl : PersistService {

    /**
     * ConfigInfo缓存
     */
    private val configInfoCache = ConcurrentHashMap<String, ConfigInfo>()


    override fun findConfigInfo(dataId: String, group: String, tenant: String): ConfigInfo? {
        // TODO, 暂时先这么玩, 后面再说
        val key = getKeyTenant(dataId, group, tenant)
        return configInfoCache[key]
    }

    override fun insertOrUpdate(
        srcIp: String,
        srcUser: String,
        configInfo: ConfigInfo,
        time: Long,
        advanceConfigInfo: Map<String, Any>,
        notify: Boolean
    ) {
        // TODO, 暂时先这么玩
        val key = getKeyTenant(configInfo.dataId ?: "", configInfo.group ?: "", configInfo.tenant ?: "")
        configInfoCache[key] = configInfo
    }
}