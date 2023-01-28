package com.wanna.boot.autoconfigure.jsonb

import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.framework.context.annotation.Configuration

/**
 * Jsonb的自动配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
@ConditionalOnClass(name = ["javax.json.bind.Jsonb"])
@Configuration(proxyBeanMethods = false)
open class JsonbAutoConfiguration {

}