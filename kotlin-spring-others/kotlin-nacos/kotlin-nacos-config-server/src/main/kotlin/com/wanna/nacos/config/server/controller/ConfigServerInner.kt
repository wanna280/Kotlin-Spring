package com.wanna.nacos.config.server.controller

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.stereotype.Service
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.nacos.config.server.service.LongPollingService

/**
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
        if (LongPollingService.isSupportLongPolling(request)) {
            longPollingService.addLongPollingClient(request, response, clientMd5Map, probeRequestSize)
            return "200"
        }

        return "200"
    }
}