package com.wanna.boot.context.config

import com.wanna.boot.cloud.CloudPlatform
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.lang.Nullable

/**
 * ConfigData的ActivationContext
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
class ConfigDataActivationContext(
    @Nullable val cloudPlatform: CloudPlatform?,
    @Nullable val profiles: Profiles? = null
) {

    constructor(environment: Environment, binder: Binder) : this(null, null)


    /**
     * 携带profiles的方式去进行构建
     *
     * @param profiles profiles
     * @return 构建出来的新的含有Profiles的ConfigDataActivationContext
     */
    fun withProfiles(profiles: Profiles): ConfigDataActivationContext =
        ConfigDataActivationContext(cloudPlatform, profiles)

}