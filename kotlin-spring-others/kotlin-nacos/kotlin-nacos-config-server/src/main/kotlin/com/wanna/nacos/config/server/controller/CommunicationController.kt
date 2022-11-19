package com.wanna.nacos.config.server.controller

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.bind.annotation.RequestParam
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.nacos.config.server.service.dump.DumpService
import com.wanna.nacos.config.server.service.notify.NotifyService

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
@RequestMapping(["/v1/cs/communication"])
@RestController
class CommunicationController {

    @Autowired
    private lateinit var dumpService: DumpService

    /**
     * 通知配置文件已经发生变化...
     *
     * @param request request
     */
    @RequestMapping(["/dataChange"])
    fun notifyConfigInfo(
        request: HttpServerRequest,
        @RequestParam("dataId") dataId: String,
        @RequestParam("group") group: String,
        @RequestParam("tenant", required = false, defaultValue = "") tenant: String,
        @RequestParam("tag", required = false, defaultValue = "") tag: String
    ): Boolean {
        val lastModifiedStr = request.getHeader(NotifyService.NOTIFY_HEADER_LAST_MODIFIED)
        val lastModified = lastModifiedStr?.toLongOrNull() ?: -1L
        val handleIp = request.getHeader(NotifyService.NOTIFY_HEADER_OP_HANDLE_IP) ?: ""
        dumpService.dump(dataId, group, tenant, tag, lastModified, handleIp)
        return true
    }
}