package com.wanna.cloud.openfeign

import kotlin.reflect.KClass


/**
 * 标识这是一个FeignClient
 *
 * @param value FeignClientName
 * @param name FeignClientName
 * @param contextId childContextName
 * @param path 所有方法级别的映射(路径)的前缀
 * @param configuration 该context当中的配置类列表
 * @param url 请求的url(如果没有url, 将会使用FeignClientName作为serviceName)
 * @param fallback 指定FeignClient的Fallback, 指定的Class必须是一个合法的SpringBean, 并且实现标注这个注解的FeignClient接口
 * @param fallbackFactory 创建fallback的Factory
 */
annotation class FeignClient(
    val value: String = "",
    val name: String = "",
    val contextId: String = "",
    val path: String = "",
    val url: String = "",
    val configuration: Array<KClass<*>> = [],
    val fallback: KClass<*> = Void::class,
    val fallbackFactory: KClass<*> = Void::class
)
