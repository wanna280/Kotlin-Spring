package com.wanna.metric.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import com.wanna.metric.Metrics
import com.wanna.metric.utils.MetricsConfiguration
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.annotation.Nullable

/**
 * 默认的[MetricServer]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
open class DefaultMetricServer : MetricServer {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(DefaultMetricServer::class.java)

        /**
         * Item常量
         */
        private const val ITEM_KEY = "item"

        /**
         * 暴露Metrics监控指标的路径
         */
        private const val METRICS_URL = "/metrics"

        /**
         * 暴露单个Metrics监控指标的路径
         */
        private const val METRIC_ITEM_URL = "/metrics/item"
    }

    /**
     * HttpServer
     */
    @Nullable
    private var server: HttpServer? = null

    /**
     * 监听[MetricServer]生命周期的Listener
     */
    private val listeners = ArrayList<MetricServerListener>()

    /**
     * 添加一个[MetricServerListener], 去监听[MetricServer]的启动
     *
     * @param listener MetricServerListener
     */
    override fun addListener(listener: MetricServerListener) {
        this.listeners += listener
    }

    /**
     * 启动MetricServer
     */
    override fun start() {
        // callback所有的Listener去进行init
        for (listener in listeners) {
            try {
                listener.init(this)
            } catch (ex: Exception) {
                logger.error("MetricServerListener [{}] init failed...", listener.javaClass.name, ex)
            }
        }

        val server = HttpServer.create(InetSocketAddress(MetricsConfiguration.metricServerPort), 0)

        // 添加处理所有的监控指标的暴露的Handler
        server.createContext(METRICS_URL, MetricsHandler())

        // 添加处理单个监控指标的暴露的Handler
        server.createContext(METRIC_ITEM_URL, MetricItemHandler())

        // start MetricServer
        server.start()
        this.server = server
    }

    /**
     * 关闭MetricServer
     */
    override fun stop() {
        // callback所有的Listener去进行destroy
        for (listener in listeners) {
            try {
                listener.destroy(this)
            } catch (ex: Exception) {
                logger.error("MetricServerListener [{}] destroy failed...", listener.javaClass.name, ex)
            }
        }

        // stop MetricServer
        this.server?.stop(0)
    }

    /**
     * 将响应数据去写入到response当中
     *
     * @param result 需要去进行写入的数据
     * @param exchange HttpExchange
     */
    private fun writeToResponse(result: CharSequence, exchange: HttpExchange) {
        // response status code
        exchange.sendResponseHeaders(200, 0)
        exchange.responseBody.use {
            it.write(result.toString().toByteArray(StandardCharsets.UTF_8))
        }
    }

    /**
     * 从[HttpExchange]当中去提取到Query参数
     *
     * @param exchange Http Exchange
     * @return 从HttpExchange当中提取得到的参数列表, 使用"&"作为Element分隔符, 使用"="作为K-V分隔符
     */
    private fun getQueryParams(exchange: HttpExchange): Map<String, String> {
        val queryParams = URLDecoder.decode(exchange.requestURI.query ?: "", StandardCharsets.UTF_8) ?: ""
        return queryParams.split("&").map { it.split("=") }.filter { it.size == 2 }.associate { it[0] to it[1] }
    }

    /**
     * 提供暴露Metrics指标的Handler
     */
    private inner class MetricsHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            val result = StringBuilder()
            for (item in Metrics.getCurrentItems()) {
                result.append(item.key).append("=").append(item.value.toString()).append("\n")
            }
            writeToResponse(result, exchange)
        }
    }

    /**
     * 暴露单个MetricItem的Handler
     */
    private inner class MetricItemHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            val params = getQueryParams(exchange)
            val item = params[ITEM_KEY]
            val value = if (item == null) "" else Metrics.getCurrentItems()[item].toString() ?: ""

            // 对于item参数没给, 或者获取到的item为空的情况, 那么value都为""
            writeToResponse(value, exchange)
        }
    }

}