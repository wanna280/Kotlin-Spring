package com.wanna.boot.autoconfigure.gson

import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.context.annotation.Configuration

/**
 * Gson的自动配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
@ConditionalOnClass(name = ["com.google.gson.Gson"])
@EnableConfigurationProperties(value = [GsonProperties::class])
@Configuration(proxyBeanMethods = false)
open class GsonAutoConfiguration {

}