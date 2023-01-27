package com.wanna.boot.web.client

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.web.client.ResponseErrorHandler
import com.wanna.framework.web.client.RestTemplate
import com.wanna.framework.web.http.client.ClientHttpRequest
import com.wanna.framework.web.http.client.ClientHttpRequestFactory
import com.wanna.framework.web.http.client.ClientHttpRequestInterceptor
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.util.UriTemplateHandler
import java.time.Duration
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

/**
 * [RestTemplate]的Builder, 基于Builder的方式去进行[RestTemplate]的构建
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @param settings [ClientHttpRequestFactory]的配置信息(读超时时间/连接超时时间/是否需要用缓冲流去缓存RequestBody)
 * @param interceptors 对请求进行拦截的拦截器
 * @param messageConverters Http MessageConverters, 提供消息(RequestBody/ResponseBody)的转换
 * @param uriTemplateHandler URI TemplateHandler
 * @param customizers 对[RestTemplate]去进行自定义的自定义化器
 * @param requestCustomizers 对于[ClientHttpRequest]去进行自定义的自定义化器
 * @param errorHandler 响应处理的错误的异常处理器
 * @param requestFactory 构建[ClientHttpRequestFactory]的工厂方法(参数是Setting配置信息)
 * @param detectRequestFactory 是否需要去进行自动探测合适的[ClientHttpRequestFactory]?
 */
open class RestTemplateBuilder private constructor(
    private val settings: ClientHttpRequestFactorySettings,
    private val interceptors: Set<ClientHttpRequestInterceptor>,
    private val messageConverters: Collection<HttpMessageConverter<*>>,
    @Nullable private val uriTemplateHandler: UriTemplateHandler?,
    private val customizers: Collection<RestTemplateCustomizer>,
    private val requestCustomizers: Set<RestTemplateRequestCustomizer<*>>,
    @Nullable private val errorHandler: ResponseErrorHandler? = null,
    @Nullable private val requestFactory: Function<ClientHttpRequestFactorySettings, ClientHttpRequestFactory>? = null,
    private val detectRequestFactory: Boolean = true
) {

    /**
     * 无参数构造器
     */
    constructor() : this(
        ClientHttpRequestFactorySettings.DEFAULTS, emptySet(), emptyList(), null,
        emptyList(), emptySet(), null, null, true
    )

    // requestFactory

    open fun requestFactory(requestFactoryType: Class<out ClientHttpRequestFactory>): RestTemplateBuilder {
        return requestFactory(Function { ClientHttpRequestFactories.get(requestFactoryType, it) })
    }

    open fun requestFactory(requestFactory: Supplier<ClientHttpRequestFactory>): RestTemplateBuilder {
        return requestFactory(Function { ClientHttpRequestFactories.get(requestFactory, it) })
    }

    open fun requestFactory(requestFactory: Function<ClientHttpRequestFactorySettings, ClientHttpRequestFactory>): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, interceptors, messageConverters, uriTemplateHandler,
            customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }


    // messageConverters and additional messageConverters

    open fun defaultMessageConverters(): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, interceptors, RestTemplate().getHttpMessageConverters(), uriTemplateHandler,
            customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    open fun messageConverters(vararg messageConverters: HttpMessageConverter<*>): RestTemplateBuilder {
        return messageConverters(messageConverters.toSet())
    }

    open fun messageConverters(messageConverters: Collection<HttpMessageConverter<*>>): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, interceptors, messageConverters, uriTemplateHandler,
            customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    open fun additionalMessageConverters(vararg messageConverters: HttpMessageConverter<*>): RestTemplateBuilder {
        return additionalMessageConverters(messageConverters.toSet())
    }

    open fun additionalMessageConverters(messageConverters: Collection<HttpMessageConverter<*>>): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, interceptors, append(this.messageConverters, messageConverters), uriTemplateHandler,
            customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    // customizers and additional customizers

    open fun customizers(vararg customizers: RestTemplateCustomizer): RestTemplateBuilder {
        return customizers(customizers.toSet())
    }

    open fun customizers(customizers: Collection<RestTemplateCustomizer>): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, interceptors, messageConverters, uriTemplateHandler,
            customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    open fun additionalCustomizers(vararg customizers: RestTemplateCustomizer): RestTemplateBuilder {
        return additionalCustomizers(customizers.toSet())
    }

    open fun additionalCustomizers(customizers: Collection<RestTemplateCustomizer>): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, interceptors, messageConverters, uriTemplateHandler, append(this.customizers, customizers),
            requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    // requestCustomizers and additional requestCustomizers

    open fun requestCustomizers(vararg requestCustomizers: RestTemplateRequestCustomizer<*>): RestTemplateBuilder {
        return requestCustomizers(requestCustomizers.toSet())
    }

    open fun requestCustomizers(requestCustomizers: Collection<RestTemplateRequestCustomizer<*>>): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, interceptors, messageConverters, uriTemplateHandler,
            customizers, requestCustomizers.toSet(), errorHandler, requestFactory, detectRequestFactory
        )
    }

    open fun additionalRequestCustomizers(vararg requestCustomizers: RestTemplateRequestCustomizer<*>): RestTemplateBuilder {
        return additionalRequestCustomizers(requestCustomizers.toSet())
    }

    open fun additionalRequestCustomizers(requestCustomizers: Collection<RestTemplateRequestCustomizer<*>>): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, interceptors, messageConverters, uriTemplateHandler, customizers,
            append(this.requestCustomizers, requestCustomizers), errorHandler, requestFactory, detectRequestFactory
        )
    }

    // interceptors and additional interceptors

    open fun interceptors(vararg interceptors: ClientHttpRequestInterceptor): RestTemplateBuilder {
        return interceptors(interceptors.toSet())
    }

    open fun interceptors(interceptors: Collection<ClientHttpRequestInterceptor>): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, interceptors.toSet(), messageConverters, uriTemplateHandler,
            customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    open fun additionalInterceptors(vararg interceptors: ClientHttpRequestInterceptor): RestTemplateBuilder {
        return additionalInterceptors(interceptors.toSet())
    }

    open fun additionalInterceptors(interceptors: Collection<ClientHttpRequestInterceptor>): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, append(this.interceptors, interceptors), messageConverters,
            uriTemplateHandler, customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    // uriTemplateHandler

    open fun uriTemplateHandler(uriTemplateHandler: UriTemplateHandler): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings, this.interceptors, messageConverters,
            uriTemplateHandler, customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    // readTimeout

    open fun setReadTimeout(readTimeout: Duration): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings.withReadTimeout(readTimeout), this.interceptors, messageConverters,
            uriTemplateHandler, customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    // connectionTimout

    open fun setConnectionTimeout(connectionTimeout: Duration): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings.withConnectionTimeout(connectionTimeout), this.interceptors, messageConverters,
            uriTemplateHandler, customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }

    // bufferRequestBody

    open fun setBufferRequestBody(bufferRequestBody: Boolean): RestTemplateBuilder {
        return RestTemplateBuilder(
            settings.withBufferRequestBody(bufferRequestBody), this.interceptors, messageConverters,
            uriTemplateHandler, customizers, requestCustomizers, errorHandler, requestFactory, detectRequestFactory
        )
    }


    // build

    open fun build(): RestTemplate {
        return configure(RestTemplate())
    }

    open fun <T : RestTemplate> build(restTemplateClass: Class<T>): T {
        return configure(BeanUtils.instantiateClass(restTemplateClass))
    }

    open fun <T : RestTemplate> configure(restTemplate: T): T {
        val requestFactory = buildRequestFactory()

        // 设置RequestFactory
        if (requestFactory != null) {
            restTemplate.setRequestFactory(requestFactory)
        }

        // 将Request Customizer等配置去应用给RestTemplate
        addClientHttpRequestInitializer(restTemplate)

        // 设置ResponseErrorHandler
        if (errorHandler != null) {
            restTemplate.errorHandler = errorHandler
        }

        // 设置MessageConverters
        if (messageConverters.isNotEmpty()) {
            restTemplate.setHttpMessageConverters(messageConverters)
        }

        // 设置URI TemplateHandler
        if (this.uriTemplateHandler != null) {
            restTemplate.uriTemplateHandler = this.uriTemplateHandler
        }

        // 添加拦截器列表
        restTemplate.getInterceptors().addAll(this.interceptors)

        // 利用Customizer, 去对RestTemplate去进行自定义
        if (customizers.isNotEmpty()) {
            for (customizer in customizers) {
                customizer.customize(restTemplate)
            }
        }
        return restTemplate
    }

    /**
     * 构建[ClientHttpRequestFactory]
     *
     * @return ClientHttpRequestFactory
     */
    @Nullable
    open fun buildRequestFactory(): ClientHttpRequestFactory? {
        // 如果指定了构建ClientHttpRequestFactory的工厂方法, 那么使用它去进行构建
        if (this.requestFactory != null) {
            return requestFactory.apply(this.settings)
        }

        // 如果启用了自动探测ClientHttpRequestFactory
        if (this.detectRequestFactory) {
            return ClientHttpRequestFactories.get(settings)
        }
        return null
    }

    private fun addClientHttpRequestInitializer(restTemplate: RestTemplate) {
        if (this.requestCustomizers.isEmpty()) {
            return
        }
        restTemplate.getClientHttpRequestInitializers()
            .add(RestTemplateBuilderClientHttpRequestInitializer(this.requestCustomizers))
    }

    private fun <T> append(collection: Collection<T>, additions: Collection<T>): Set<T> {
        val result = LinkedHashSet(collection)
        result.addAll(additions)
        return Collections.unmodifiableSet(result)
    }
}