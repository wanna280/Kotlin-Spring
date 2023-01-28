package com.wanna.boot.autoconfigure.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.framework.context.annotation.Configuration

/**
 * Jackson的[HttpMessageConverters]的配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
@ConditionalOnClass([ObjectMapper::class])
@Configuration(proxyBeanMethods = false)
open class JacksonHttpMessageConvertersConfiguration {

}