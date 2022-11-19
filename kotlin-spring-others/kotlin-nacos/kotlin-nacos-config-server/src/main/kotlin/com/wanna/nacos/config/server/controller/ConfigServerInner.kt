package com.wanna.nacos.config.server.controller

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.stereotype.Service
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpStatus
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.nacos.config.server.enums.FileTypeEnum
import com.wanna.nacos.config.server.model.ConfigInfo
import com.wanna.nacos.config.server.model.event.ConfigDataChangeEvent
import com.wanna.nacos.config.server.service.ConfigCacheService
import com.wanna.nacos.config.server.service.ConfigChangePublisher
import com.wanna.nacos.config.server.service.LongPollingService
import com.wanna.nacos.config.server.service.repository.PersistService
import com.wanna.nacos.config.server.utils.GroupKey2
import com.wanna.nacos.config.server.utils.GroupKey2.getKeyTenant
import com.wanna.nacos.config.server.utils.MD5Utils

/**
 * 执行真正的操作(查询ConfigServer配置文件/添加Listener长轮询任务)的Service
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
@Service
open class ConfigServerInner {

    /**
     * 执行长轮询任务的Service
     */
    @Autowired
    private lateinit var longPollingService: LongPollingService

    /**
     * PersistService
     */
    @Autowired
    private lateinit var persistService: PersistService


    /**
     * 执行长轮询, 添加一个长轮询客户端
     *
     * @param request request
     * @param response response
     * @param clientMd5Map md5Map
     * @param probeRequestSize probeRequestSize
     */
    open fun doLongPolling(
        request: HttpServerRequest,
        response: HttpServerResponse,
        clientMd5Map: Map<String, String>,
        probeRequestSize: Int
    ): String {

        // 如果支持长轮询的话, 添加一个LongPollingClient到LongPollingService当中
        if (LongPollingService.isSupportLongPolling(request)) {
            longPollingService.addLongPollingClient(request, response, clientMd5Map, probeRequestSize)
            return "200"
        }
        // 如果不支持长轮询的话, 那么需要使用短轮询的方式去进行实现
        return doShortPolling(request, response, clientMd5Map, probeRequestSize)
    }

    /**
     * 短轮询的实现
     *
     * @param request request
     * @param response response
     * @param clientMd5Map clientMd5Map(Key-groupKey, Value-clientMd5)
     * @param probeRequestSize probeRequestSize
     */
    private fun doShortPolling(
        request: HttpServerRequest,
        response: HttpServerResponse,
        clientMd5Map: Map<String, String>,
        probeRequestSize: Int
    ): String {
        val changedGroups = MD5Utils.compareMd5(request, response, clientMd5Map)
        response.setStatus(HttpStatus.SUCCESS)
        return "200"
    }

    /**
     * 真正地去执行一个配置文件(ConfigInfo)的发布, 如果已经存在的话, 那么是更新; 如果不存在的话, 那么是新增
     *
     * @param request request
     * @param response response
     * @param configInfo ConfigInfo, 维护的是一个配置文件的相关信息(content/md5/dataId/group/tenant)
     * @param srcUser srcUser
     * @param srcIp srcIp
     */
    open fun doPublishConfig(
        request: HttpServerRequest,
        response: HttpServerResponse,
        configInfo: ConfigInfo,
        srcUser: String,
        srcIp: String
    ): String {
        persistService.insertOrUpdate(srcIp, srcUser, configInfo, System.currentTimeMillis(), emptyMap(), true)

        // 通知ConfigServer本地的Listener, 配置文件已经发生变更了...
        ConfigChangePublisher.notifyConfigChange(
            ConfigDataChangeEvent(
                configInfo.dataId ?: "",
                configInfo.group ?: "",
                configInfo.tenant ?: "",
                System.currentTimeMillis()
            )
        )
        response.flush()  // flush
        return "200"
    }

    /**
     * 真正地去执行根据dataId&group&tag&clientIp获取ConfigServer当中的配置文件
     *
     * @param request request
     * @param response response
     * @param dataId dataId
     * @param group group
     * @param tenant tenant
     * @param tag tag
     * @param clientIp clientIp
     */
    open fun doGetConfig(
        request: HttpServerRequest,
        response: HttpServerResponse,
        dataId: String,
        group: String,
        tenant: String,
        tag: String,
        clientIp: String
    ): String {

        // 从ConfigCache当中去获取到对应的缓存的ConfigItem信息
        val cacheItem = ConfigCacheService.getContentCache(getKeyTenant(dataId, group, tenant))

        // TODO 从Disk/PersistentService去读取到配置文件
        val configInfo = persistService.findConfigInfo(dataId, group, tenant)

        if (cacheItem != null) {

            // 如果为空的话, 那么默认为"TEXT"
            val fileType = cacheItem.type.ifBlank { FileTypeEnum.TEXT.fileType }

            // 设置Config-Type, 配置文件类型
            response.addHeader("Config-Type", fileType)

            // 获取到Config-Type对应的Content-Type, 也就是响应给客户端的数据格式(TEXT/JSON...)
            val fileTypeEnum = FileTypeEnum.getFileTypeEnumByFileExtensionOrFileType(fileType)
            response.setHeader(HttpHeaders.CONTENT_TYPE, fileTypeEnum.contentType)


        }

        val content = configInfo?.content
        if (content != null) {
            response.getOutputStream().write(content.toByteArray())
            response.flush()
        }

        return "200"
    }

    open fun doRemoveConfig(
        request: HttpServerRequest,
        response: HttpServerResponse,
        dataId: String,
        group: String,
        tenant: String,
        tag: String,
        srcIp: String,
        srcUser: String
    ) {
        persistService.removeConfigInfo(dataId, group, tenant, srcIp, srcUser)
    }
}