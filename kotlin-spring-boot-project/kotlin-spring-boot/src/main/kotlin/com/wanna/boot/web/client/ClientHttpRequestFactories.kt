package com.wanna.boot.web.client

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.web.http.client.ClientHttpRequestFactory
import com.wanna.framework.web.http.client.HttpComponentsClientHttpRequestFactory
import com.wanna.framework.web.http.client.SimpleClientHttpRequestFactory
import org.apache.http.client.HttpClient
import org.apache.http.config.SocketConfig
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import java.lang.reflect.Method
import java.time.Duration
import java.util.function.Supplier

/**
 * 提供[ClientHttpRequestFactory]的创建的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @see HttpComponentsClientHttpRequestFactory
 * @see SimpleClientHttpRequestFactory
 */
object ClientHttpRequestFactories {

    /**
     * Apache的HttpClient的类
     */
    private const val APACHE_HTTP_CLIENT_CLASS = "org.apache.http.client.HttpClient"

    /**
     * Apache的HttpClient是否存在?
     */
    @JvmStatic
    private val APACHE_HTTP_CLIENT_PRESENT = ClassUtils.isPresent(APACHE_HTTP_CLIENT_CLASS)

    /**
     * 探测相关HttpClient的依赖是否存在, 从而使用不同的[ClientHttpRequestFactory]
     *
     * @param settings ClientFactory的配置信息
     * @return 创建得到的[ClientHttpRequestFactory]实例
     */
    @JvmStatic
    fun get(settings: ClientHttpRequestFactorySettings): ClientHttpRequestFactory {
        if (APACHE_HTTP_CLIENT_PRESENT) {
            return HttpComponents.get(settings)
        }
        return Simple.get(settings)
    }

    /**
     * 根据[clientFactoryType]去获取到合适的[ClientHttpRequestFactory]
     *
     * @param clientFactoryType ClientFactory类型
     * @param settings ClientFactory的配置信息
     * @return 创建得到的[ClientHttpRequestFactory]实例
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : ClientHttpRequestFactory> get(clientFactoryType: Class<T>, settings: ClientHttpRequestFactorySettings): T {
        if (clientFactoryType == ClientHttpRequestFactory::class.java) {
            return get(settings) as T
        }
        if (clientFactoryType == HttpComponentsClientHttpRequestFactory::class.java) {
            return HttpComponents.get(settings) as T
        }
        if (clientFactoryType == SimpleClientHttpRequestFactory::class.java) {
            return Simple.get(settings) as T
        }
        // 基于反射去创建ClientHttpRequestFactory
        return get({ createRequestFactory(clientFactoryType) }, settings)
    }

    /**
     * 基于给定的[clientFactorySupplier]去创建[ClientHttpRequestFactory], 并使用反射的方式去进行配置信息的应用
     *
     * @param clientFactorySupplier ClientFactory的Supplier
     * @param settings ClientFactory的配置信息
     * @return 创建得到的[ClientHttpRequestFactory]实例
     */
    @JvmStatic
    fun <T : ClientHttpRequestFactory> get(
        clientFactorySupplier: Supplier<T>,
        settings: ClientHttpRequestFactorySettings
    ): T {
        return Reflective.get(clientFactorySupplier, settings)
    }

    private fun <T : ClientHttpRequestFactory> createRequestFactory(requestFactory: Class<T>): T {
        try {
            val constructor = requestFactory.getDeclaredConstructor()
            ReflectionUtils.makeAccessible(constructor)
            return constructor.newInstance()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
    }

    /**
     * 构建[HttpComponentsClientHttpRequestFactory]的工厂对象
     */
    private object HttpComponents {

        /**
         * 基于[ClientHttpRequestFactorySettings]去构建[HttpComponentsClientHttpRequestFactory]
         *
         * @param settings settings
         * @return HttpComponentsClientHttpRequestFactory
         */
        @JvmStatic
        fun get(settings: ClientHttpRequestFactorySettings): HttpComponentsClientHttpRequestFactory {
            val requestFactory = createRequestFactory(settings.readTimeout)
            if (settings.connectionTimeout != null) {
                requestFactory.connectTimeout = settings.connectionTimeout.toMillis().toInt()
            }
            if (settings.bufferRequestBody != null) {
                requestFactory.bufferRequestBody = settings.bufferRequestBody
            }
            return requestFactory
        }

        /**
         * 创建一个[HttpComponentsClientHttpRequestFactory]
         *
         * @param readTimeout 读超时时间
         * @return Apache HttpClient
         */
        @JvmStatic
        private fun createRequestFactory(@Nullable readTimeout: Duration?): HttpComponentsClientHttpRequestFactory {
            return if (readTimeout != null) HttpComponentsClientHttpRequestFactory(createHttpClient(readTimeout))
            else HttpComponentsClientHttpRequestFactory()
        }


        /**
         * 创建Apache的[HttpClient]
         *
         * @param readTimeout 读超时时间
         * @return Apache HttpClient
         */
        @JvmStatic
        private fun createHttpClient(readTimeout: Duration): HttpClient {
            val socketConfig = SocketConfig.custom().setSoTimeout(readTimeout.toMillis().toInt()).build()
            val connectionManager = PoolingHttpClientConnectionManager()
            connectionManager.defaultSocketConfig = socketConfig
            return HttpClientBuilder.create().setConnectionManager(connectionManager).build()
        }
    }

    /**
     * 构建[SimpleClientHttpRequestFactory]的工厂对象
     */
    private object Simple {
        /**
         * 基于[ClientHttpRequestFactorySettings]去构建[SimpleClientHttpRequestFactory]
         *
         * @param settings settings
         * @return SimpleClientHttpRequestFactory
         */
        @JvmStatic
        fun get(settings: ClientHttpRequestFactorySettings): SimpleClientHttpRequestFactory {
            val requestFactory = SimpleClientHttpRequestFactory()
            if (settings.readTimeout != null) {
                requestFactory.readTimeout = settings.readTimeout.toMillis().toInt()
            }
            if (settings.connectionTimeout != null) {
                requestFactory.connectTimeout = settings.connectionTimeout.toMillis().toInt()
            }
            if (settings.bufferRequestBody != null) {
                requestFactory.bufferRequestBody = settings.bufferRequestBody
            }
            return requestFactory
        }
    }

    /**
     * 基于反射去对[ClientHttpRequestFactory]配置信息去进行设置
     */
    private object Reflective {

        @JvmStatic
        fun <T : ClientHttpRequestFactory> get(
            requestFactorySupplier: Supplier<T>,
            settings: ClientHttpRequestFactorySettings
        ): T {
            // 使用Supplier去获取到RequestFactory
            val requestFactory = requestFactorySupplier.get()

            // 对RequestFactory的配置信息去执行自定义...
            configure(requestFactory, settings)
            return requestFactory
        }

        /**
         * 基于反射调用[ClientHttpRequestFactory]的Setter, 从而完成配置信息的设置
         *
         * @param requestFactory RequestFactory
         * @param settings settings
         */
        private fun configure(requestFactory: ClientHttpRequestFactory, settings: ClientHttpRequestFactorySettings) {
            if (settings.connectionTimeout != null) {
                setConnectTimeout(requestFactory, settings.connectionTimeout)
            }
            if (settings.readTimeout != null) {
                setReadTimeout(requestFactory, settings.readTimeout)
            }
            if (settings.bufferRequestBody != null) {
                setBufferRequestBody(requestFactory, settings.bufferRequestBody)
            }
        }

        private fun setConnectTimeout(requestFactory: ClientHttpRequestFactory, connectionTimeout: Duration) {
            val method = findMethod(requestFactory, "setConnectionTimeout", Int::class.java)
            invokeMethod(requestFactory, method, connectionTimeout.toMillis())
        }

        private fun setReadTimeout(requestFactory: ClientHttpRequestFactory, readTimeout: Duration) {
            val method = findMethod(requestFactory, "setReadTimeout", Int::class.java)
            invokeMethod(requestFactory, method, readTimeout.toMillis())
        }

        private fun setBufferRequestBody(requestFactory: ClientHttpRequestFactory, bufferRequestBody: Boolean) {
            val method = findMethod(requestFactory, "setBufferRequestBody", Boolean::class.java)
            invokeMethod(requestFactory, method, bufferRequestBody)
        }

        private fun findMethod(
            requestFactory: ClientHttpRequestFactory,
            methodName: String,
            vararg parameters: Class<*>
        ): Method {
            val method = ReflectionUtils.findMethod(requestFactory::class.java, methodName, *parameters)
                ?: throw IllegalStateException("Request factory ${requestFactory.javaClass.name} does not have a suitable $methodName method")

            if (method.isAnnotationPresent(Deprecated::class.java)) {
                throw IllegalStateException("Request factory ${requestFactory.javaClass.name} has the $methodName method marked as deprecated")
            }
            return method
        }

        /**
         * 反射执行目标方法
         *
         * @param requestFactory RequestFactory
         * @param method 要执行的目标方法
         * @param parameters 执行目标方法需要的参数列表
         */
        private fun invokeMethod(requestFactory: ClientHttpRequestFactory, method: Method, vararg parameters: Any?) {
            ReflectionUtils.invokeMethod(method, requestFactory, *parameters)
        }
    }
}