package com.wanna.nacos.config.server.controller

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.web.bind.annotation.*
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.config.server.model.ConfigInfo
import com.wanna.nacos.config.server.utils.MD5Utils
import com.wanna.nacos.config.server.utils.MD5Utils.getClientMd5Map
import com.wanna.nacos.config.server.utils.NamespaceUtils
import com.wanna.nacos.config.server.utils.NamespaceUtils.processNamespaceParameter
import com.wanna.nacos.config.server.utils.RequestUtils
import com.wanna.nacos.config.server.utils.RequestUtils.getRemoteIp
import com.wanna.nacos.config.server.utils.RequestUtils.getSrcUserName
import java.net.URLDecoder

/**
 * 提供操作ConfigServer当中的配置文件的Controller
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
@RequestMapping([Constants.CONFIG_CONTROLLER_PATH])
@RestController
open class ConfigController {

    /**
     * 真正地去执行配置的发布/获取/删除、轮询任务的添加的组件
     */
    @Autowired
    private lateinit var configServerInner: ConfigServerInner

    /**
     * 发布一个配置文件
     *
     * @param request request
     * @param response response
     * @param dataId dataId
     * @param group group
     * @param tenant namespace
     * @param content fileContent
     * @param type fileType
     */
    @PostMapping
    fun publishConfig(
        request: HttpServerRequest, response: HttpServerResponse,
        @RequestParam dataId: String, @RequestParam group: String,
        @RequestParam(required = false, defaultValue = "") tenant: String, @RequestParam content: String,
        @RequestParam(required = false, defaultValue = Constants.DEFAULT_CONFIG_TYPE) type: String,
    ) {
        val srcUser = getSrcUserName(request)
        val configInfo = ConfigInfo(dataId, group, content)
        configInfo.type = type //  fileType
        configInfo.tenant = processNamespaceParameter(tenant)  // namespace
        configServerInner.doPublishConfig(request, response, configInfo, srcUser, getRemoteIp(request))
    }

    /**
     * 根据dataId&group&tenant去获取到对应的配置文件信息
     *
     * @param request request
     * @param response response
     * @param dataId dataId
     * @param group group
     * @param tenant tenant(namespace)
     * @param tag tag
     */
    @GetMapping
    fun getConfig(
        request: HttpServerRequest,
        response: HttpServerResponse,
        @RequestParam dataId: String,
        @RequestParam group: String,
        @RequestParam(required = false, defaultValue = "") tenant: String,
        @RequestParam(required = false, defaultValue = "") tag: String
    ) {
        // Note:对于namespace="null"/namespace="public"的情况我们都将namespace去转为""
        val tenant = processNamespaceParameter(tenant)
        val remoteIp = getRemoteIp(request)

        // 使用ConfigServerInner去进行真正的处理请求
        configServerInner.doGetConfig(
            request, response, dataId, group,
            tenant, tag, remoteIp
        )
    }

    /**
     * 根据dataId&group&tenant去删除一个配置文件
     *
     * @param request request
     * @param response response
     * @param dataId dataId
     * @param group group
     * @param tenant tenant(namespace)
     * @param tag tag
     */
    @DeleteMapping
    fun deleteConfig(
        request: HttpServerRequest,
        response: HttpServerResponse,
        @RequestParam("dataId") dataId: String,
        @RequestParam("group") group: String,
        @RequestParam("tenant", required = false, defaultValue = "") tenant: String,
        @RequestParam("tag", required = false, defaultValue = "") tag: String
    ): Boolean {
        // Note:对于namespace="null"/namespace="public"的情况我们都将namespace去转为""
        val tenant = processNamespaceParameter(tenant)
        val remoteIp = getRemoteIp(request)
        val srcUserName = getSrcUserName(request)
        configServerInner.doRemoveConfig(request, response, dataId, group, tenant, tag, remoteIp, srcUserName)
        return true
    }

    /**
     * 添加一个Listener监听配置信息的变化, 如果配置文件已经发生了变化的话, 那么就需要通知发送这个请求的客户端
     *
     * @param request request
     * @param response response
     */
    @PostMapping(["/listener"])
    fun listener(request: HttpServerRequest, response: HttpServerResponse) {
        // 从request当中解析出来需要进行探测的配置文件的clientMd5Map
        var probeModify = request.getParam(Constants.LISTENING_CONFIGS)
        if (probeModify == null || probeModify.isBlank()) {
            throw IllegalArgumentException("不合法的probeModify")
        }
        probeModify = URLDecoder.decode(probeModify, com.wanna.nacos.config.server.constant.Constants.ENCODE_UTF8)
        val clientMd5Map: Map<String, String>
        try {
            clientMd5Map = getClientMd5Map(probeModify)
        } catch (ex: Throwable) {
            throw IllegalArgumentException("probeModify格式不对")
        }

        // 添加一个长轮询任务
        configServerInner.doLongPolling(request, response, clientMd5Map, probeModify.length)
    }
}