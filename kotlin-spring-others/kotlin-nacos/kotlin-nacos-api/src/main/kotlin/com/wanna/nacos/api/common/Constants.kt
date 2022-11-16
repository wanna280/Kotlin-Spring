package com.wanna.nacos.api.common

object Constants {

    /**
     * 默认的ClusterName
     */
    const val DEFAULT_CLUSTER_NAME = "DEFAULT"

    /**
     * 默认的namespaceId
     */
    const val DEFAULT_NAMESPACE_ID = "public"

    /**
     * ConfigType
     */
    const val CONFIG_TYPE = "Config-Type"

    /**
     * Encrypted-Data-Key
     */
    const val ENCRYPTED_DATA_KEY = "Encrypted-Data-Key"

    /**
     * ConfigService的长轮询的默认超时时间为30s
     */
    const val CONFIG_LONG_POLL_TIMEOUT = 30000L

    /**
     * ConfigService的长轮询的最短时间为10s
     */
    const val MIN_CONFIG_LONG_POLL_TIMEOUT = 10000L
}
