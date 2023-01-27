package com.wanna.boot.autoconfigure.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.framework.context.annotation.Configuration

/**
 * Jackson的自动配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
@ConditionalOnClass(value = [ObjectMapper::class])
@Configuration(proxyBeanMethods = false)
open class JacksonAutoConfiguration {

}