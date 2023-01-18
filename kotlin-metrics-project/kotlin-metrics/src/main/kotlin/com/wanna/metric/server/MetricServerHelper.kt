package com.wanna.metric.server

import com.wanna.metric.server.MetricServerHelper.load
import com.wanna.metric.server.MetricServerHelper.loadMetricServerListeners
import com.wanna.metric.utils.MetricsConfiguration
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.Nullable

/**
 * 维护[MetricServer]的工具类, 通过单例的方式去实现,
 * 支持使用SPI机制去进行加载[MetricServerListener], 去监听[MetricServer]的生命周期
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @see load
 * @see loadMetricServerListeners
 */
object MetricServerHelper {

    /**
     * Logger
     */
    @JvmStatic
    private val logger = LoggerFactory.getLogger(MetricServerHelper::class.java)

    /**
     * Java SPI去加载服务的位置为"META-INF/services/"
     *
     * @see ServiceLoader.load
     */
    private const val SPI_LOCATION = "META-INF/services/"

    /**
     * 要去进行启动的[MetricServer], 当load完成之后会自动将[MetricServer]去保存到这里
     */
    @JvmStatic
    @Nullable
    private var server: MetricServer? = null

    /**
     * [MetricServer]是否已经加载过的状态标识位? 避免产生重复启动的情况
     */
    @JvmStatic
    private val loaded = AtomicBoolean()

    /**
     * 加载[MetricServerListener], 引导并启动[MetricServer]
     *
     * @see MetricServer
     * @see MetricServerListener
     */
    @JvmStatic
    fun load() {
        // CAS失败, 那么直接return...
        if (!loaded.compareAndSet(false, true)) {
            return
        }

        logger.info("metric-server: start to init...")
        val server = DefaultMetricServer()
        this.server = server
        logger.info("metric-server: start to load listener...")
        for (listener in loadMetricServerListeners()) {
            server.addListener(listener)
        }
        logger.info("metric-server: load listener successfully...")
        try {
            server.start()
        } catch (ex: Exception) {
            logger.error("start MetricServer failed...", ex)
            return  // return
        }
        logger.info("metric-server: init successfully, started on port:{}...", MetricsConfiguration.metricServerPort)

        // 添加Shutdown Hook, 当应用关闭时自动关闭MetricServer
        Runtime.getRuntime().addShutdownHook(Thread({
            if (this.server != null) {
                logger.info("metric-server: start to shutdown...")
                this.server?.stop()
                logger.info("metric-server: shutdown successfully...")
            }
        }, "metric-server-shutdown"))
    }

    /**
     * 使用SPI机制去加载[MetricServerListener]
     *
     * @return 使用SPI去加载得到的[MetricServerListener]列表
     * @see ServiceLoader.load
     */
    @JvmStatic
    private fun loadMetricServerListeners(): List<MetricServerListener> {
        logger.info("metric-server: load MetricServerListener using SPI from $SPI_LOCATION ...")

        // 使用ServiceLoader去加载到MetricServerListener
        val serverListeners = ServiceLoader.load(MetricServerListener::class.java).toMutableList()
        serverListeners.sortWith(Comparator.comparing(MetricServerListener::getOrder))

        logger.info(
            "metric-server: load MetricServerListener success, result={}",
            serverListeners.map { it.javaClass.name })
        return serverListeners
    }
}