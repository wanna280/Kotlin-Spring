package com.wanna.boot.context.config

/**
 * 封装ConfigDataLocation和ConfigDataResource的结果
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @see ConfigDataLocationResolver
 *
 * @param resource resource
 * @param location location
 * @param profileSpecific 是否是给定了对应的Profiles的时候的结果?
 */
data class ConfigDataResolutionResult(
    val resource: ConfigDataResource,
    val location: ConfigDataLocation,
    val profileSpecific: Boolean
)