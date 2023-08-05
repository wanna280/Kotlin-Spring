package com.wanna.cloud.endpoint

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.WriteOperation
import com.wanna.cloud.context.refresh.ContextRefresher

/**
 * Refresh的endpoint
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/5/29
 */
@Endpoint(id = "refresh")
open class RefreshEndpoint(private val contextRefresher: ContextRefresher) {

    /**
     * 执行Refresh, refreshEnvironment&refresh RefreshScope
     */
    @WriteOperation
    open fun refresh(): Collection<String> {
        return contextRefresher.refresh()
    }
}