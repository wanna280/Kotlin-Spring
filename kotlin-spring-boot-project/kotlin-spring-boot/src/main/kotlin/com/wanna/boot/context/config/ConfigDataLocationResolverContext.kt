package com.wanna.boot.context.config

import com.wanna.boot.ConfigurableBootstrapContext
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.framework.lang.Nullable

/**
 * ConfigDataLocationResolver进行ConfigDataLocation的解析时需要用到的上下文信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @see ConfigDataLocationResolver
 */
interface ConfigDataLocationResolverContext {

    fun getBinder(): Binder

    @Nullable
    fun getParent(): ConfigDataResource?

    fun getBootstrapContext(): ConfigurableBootstrapContext
}