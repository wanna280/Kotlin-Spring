package com.wanna.nacos.client.config.filter.impl

import com.wanna.nacos.api.config.filter.IConfigFilter
import com.wanna.nacos.api.config.filter.IConfigFilterChain
import com.wanna.nacos.api.config.filter.IConfigRequest
import com.wanna.nacos.api.config.filter.IConfigResponse
import java.util.*
import kotlin.collections.ArrayList

/**
 * [IConfigFilterChain]的管理器, 维护[IConfigFilter]的列表并控制其执行和流转
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/14
 *
 * @param properties Properties
 */
class ConfigFilterChainManager(private val properties: Properties) : IConfigFilterChain {

    /**
     * ConfigFilters
     */
    private val configFilters = ArrayList<IConfigFilter>()

    override fun doFilter(request: IConfigRequest, response: IConfigResponse) {
        VirtualFilterChain(configFilters).doFilter(request, response)
    }

    /**
     * 真正地去控制[IConfigFilter]的流转的[IConfigFilterChain]
     *
     * @param filters filters
     */
    private class VirtualFilterChain(private val filters: List<IConfigFilter>) : IConfigFilterChain {

        private var currentIndex = 0

        override fun doFilter(request: IConfigRequest, response: IConfigResponse) {
            if (currentIndex != filters.size) {
                currentIndex++
                val filter = filters[currentIndex - 1]
                filter.doFilter(request, response, this)
            }
        }
    }
}