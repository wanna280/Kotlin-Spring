package com.wanna.boot.context.config

import com.wanna.boot.ConfigurableBootstrapContext

/**
 * ConfigDataLoader进行资源的解析时用到的上下文参数信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param ConfigDataLoader
 */
interface ConfigDataLoaderContext {
    fun getBootstrapContext(): ConfigurableBootstrapContext
}