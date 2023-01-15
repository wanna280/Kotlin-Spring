package com.wanna.cloud.openfeign

import com.wanna.framework.context.annotation.Import
import kotlin.reflect.KClass

/**
 * 开启FeignClient的相关功能
 *
 * @param value 要扫描的包, 同basePackages
 * @param basePackages 要扫描的包, 同value
 * @param basePackageClasses 指定配置类, 将类的package作为包去进行扫描
 * @param clients 要去进行注册的FeignClient的类(标注了@FeignClient注解的接口)
 * @param defaultConfiguration 全部的FeignClient的默认配置类
 */
@Import([FeignClientsRegistrar::class])
annotation class EnableFeignClients(
    val value: Array<String> = [],
    val basePackages: Array<String> = [],
    val basePackageClasses: Array<KClass<*>> = [],
    val clients: Array<KClass<*>> = [],
    val defaultConfiguration: Array<KClass<*>> = []
)
