package com.wanna.nacos.config.server.controller

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.web.bind.annotation.PostMapping
import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

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