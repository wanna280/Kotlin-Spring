package com.wanna.cloud.nacos.config

import com.wanna.boot.context.properties.ConfigurationProperties
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.environment.Environment
import java.util.Properties

/**
 * 它维护了Nacos相关的配置属性
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("spring.cloud.nacos.config")
open class NacosConfigProperties {
    companion object {
        const val DEFAULT_SERVER_ADDR = "127.0.0.1:8848"
        const val DEFAULT_NACOS_GROUP = "DEFAULT_GROUP"
        const val DEFAULT_FILE_EXTENSION = "properties"
        const val DEFAULT_TIMEOUT = 5000L
    }

    var environment: Environment? = null

    var serverAddr = DEFAULT_SERVER_ADDR

    var prefix = ""

    var group = ""

    var fileExtension = ""

    var timeout = DEFAULT_TIMEOUT

    var username = ""

    var password = ""

    /**
     * 获取NacosConfigProperties, 重新生成所有的字段, 并且返回Properties
     *
     * @return Properties
     */
    open fun getNacosConfigProperties(): Properties {
        val environment = this.environment
        if (environment != null) {
            this.serverAddr =
                environment.resolvePlaceholders("${'$'}{spring.cloud.nacos.config.serverAddr:$DEFAULT_SERVER_ADDR}")!!
            this.prefix = environment.resolvePlaceholders("${'$'}{spring.cloud.nacos.config.prefix:}")!!
            this.group =
                environment.resolvePlaceholders("${'$'}{spring.cloud.nacos.config.group:$DEFAULT_NACOS_GROUP}")!!
            this.timeout =
                environment.resolvePlaceholders("${'$'}{spring.cloud.nacos.config.timeout:$DEFAULT_TIMEOUT}")!!.toLong()
            this.fileExtension =
                environment.resolveRequiredPlaceholders("${'$'}{spring.cloud.nacos.config.fileExtension:$DEFAULT_FILE_EXTENSION}")
            this.username = environment.resolveRequiredPlaceholders("${'$'}{spring.cloud.nacos.config.username:}")
            this.password = environment.resolveRequiredPlaceholders("${'$'}{spring.cloud.nacos.config.password:}")
        }
        val properties = Properties()
        properties["serverAddr"] = this.serverAddr
        properties["prefix"] = this.prefix
        properties["group"] = this.group
        properties["fileExtension"] = this.fileExtension
        properties["username"] = this.username
        properties["password"] = this.password
        properties["timeout"] = this.timeout
        return properties
    }
}