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
     * Endpoint地址
     */
    const val ENDPOINT = "endpoint"

    /**
     * Endpoint的端口号
     */
    const val ENDPOINT_PORT = "endpointPort";

    /**
     * Namespace
     */
    const val NAMESPACE = "namespace"

    /**
     * 长轮询的超时时间(默认为30s)
     */
    const val CONFIG_LONG_POLL_TIMEOUT = "configLongPollTimeout"
}