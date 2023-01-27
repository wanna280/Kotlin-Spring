package com.wanna.boot.web.client

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.http.client.ClientHttpRequestFactory
import java.time.Duration

/**
 * 维护对于[ClientHttpRequestFactory]的相关配置信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @param connectionTimeout 连接超时时间
 * @param readTimeout 读超时时间
 * @param bufferRequestBody 是否需要将RequestBody使用缓冲流去进行缓存一遍?
 */
data class ClientHttpRequestFactorySettings(
    @Nullable val connectionTimeout: Duration?,
    @Nullable val readTimeout: Duration?,
    @Nullable val bufferRequestBody: Boolean?
) {

    companion object {
        /**
         * 全部配置信息都使用默认配置信息的[ClientHttpRequestFactorySettings]实例对象
         */
        @JvmField
        val DEFAULTS = ClientHttpRequestFactorySettings(null, null, null)
    }

    fun withConnectionTimeout(connectionTimeout: Duration): ClientHttpRequestFactorySettings {
        return ClientHttpRequestFactorySettings(connectionTimeout, readTimeout, bufferRequestBody)
    }

    fun withReadTimeout(readTimeout: Duration): ClientHttpRequestFactorySettings {
        return ClientHttpRequestFactorySettings(connectionTimeout, readTimeout, bufferRequestBody)
    }

    fun withBufferRequestBody(bufferRequestBody: Boolean): ClientHttpRequestFactorySettings {
        return ClientHttpRequestFactorySettings(connectionTimeout, readTimeout, bufferRequestBody)
    }
}