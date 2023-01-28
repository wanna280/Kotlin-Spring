package com.wanna.boot.autoconfigure.http

import com.google.gson.Gson
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.framework.context.annotation.Configuration

/**
 * Gson的[HttpMessageConverters]的配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
@ConditionalOnClass([Gson::class])
@Configuration(proxyBeanMethods = false)
open class GsonHttpMessageConvertersConfiguration {

}