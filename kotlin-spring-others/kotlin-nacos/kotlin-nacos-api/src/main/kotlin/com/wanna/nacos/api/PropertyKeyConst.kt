package com.wanna.nacos.api

/**
 * 为NamingService和ConfigService去提供配置信息的相关的属性Key
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/14
 */
object PropertyKeyConst {

    /**
     * Server地址
     */
    const val SERVER_ADDR = "serverAddr"

    /**
     * Namespace
     */
    const val NAMESPACE = "namespace"

    /**
     * 长轮询的超时时间(默认为30s)
     */
    const val CONFIG_LONG_POLL_TIMEOUT = "configLongPollTimeout"
}