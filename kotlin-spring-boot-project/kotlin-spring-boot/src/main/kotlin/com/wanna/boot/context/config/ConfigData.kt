package com.wanna.boot.context.config

import com.wanna.framework.core.environment.PropertySource
import java.util.Collections

/**
 * 加载配置文件得到的PropertySource结果
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param propertySources PropertySources
 *
 * @see ConfigDataLoader
 * @see ConfigDataLoaders
 */
class ConfigData(val propertySources: List<PropertySource<*>>) {
    companion object {

        /**
         * 空的ConfigData的常量
         */
        @JvmField
        val EMPTY = ConfigData(emptyList())
    }
}