package com.wanna.nacos.config.server.controller

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.web.bind.annotation.*
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.nacos.config.server.utils.NamespaceUtils
import com.wanna.nacos.config.server.utils.NamespaceUtils.processNamespaceParameter
import com.wanna.nacos.config.server.utils.RequestUtils

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
@RequestMapping(["/v1/cs/configs"])
@RestController
open class ConfigController {
    @Autowired
    private lateinit var configServerInner: ConfigServerInner

    /**
     * 根据dataId&group&tenant去获取到对应的配置文件信息
     *
     * @param request request
     * @param response response
     * @param dataId dataId
     * @param group group
     * @param tenant
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

        // 使用ConfigServerInner去进行真正的处理请求
        // Note:对于namespace="null"/namespace="public"的情况我们都将namespace去转为""
        configServerInner.doGetConfig(
            request, response, dataId, group,
            processNamespaceParameter(tenant), tag, RequestUtils.getRemoteIp(request)
        )
    }

    /**
     * 添加一个Listener监听配置信息的变化
     *
     * @param request request
     * @param response response
     */
    @PostMapping(["/listener"])
    fun listener(request: HttpServerRequest, response: HttpServerResponse) {
        configServerInner.doLongPolling(request, response, emptyMap(), 0)
    }
}