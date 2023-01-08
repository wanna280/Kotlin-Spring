package com.wanna.boot.context.config

import com.wanna.framework.lang.Nullable

/**
 * ConfigTree的[ConfigDataLocationResolver]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
open class ConfigTreeConfigDataLocationResolver : ConfigDataLocationResolver<ConfigTreeConfigDataResource> {

    companion object {
        /**
         * 支持去处理的ConfigDataLocation的前缀
         */
        private const val PREFIX = "configtree:"
    }

    override fun isResolvable(
        @Nullable context: ConfigDataLocationResolverContext?,
        location: ConfigDataLocation
    ): Boolean = location.hasPrefix(PREFIX)

    override fun resolve(
        @Nullable context: ConfigDataLocationResolverContext?,
        location: ConfigDataLocation
    ): List<ConfigTreeConfigDataResource> {
        return emptyList()
    }
}