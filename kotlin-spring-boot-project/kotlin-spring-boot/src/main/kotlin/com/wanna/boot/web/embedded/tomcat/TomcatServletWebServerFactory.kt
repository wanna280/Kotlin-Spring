package com.wanna.boot.web.embedded.tomcat

import com.wanna.boot.web.server.ConfigurableWebServerFactory
import com.wanna.boot.web.servlet.ServletContextInitializer
import com.wanna.boot.web.servlet.ServletWebServerFactory
import org.apache.catalina.Context
import org.apache.catalina.Engine
import org.apache.catalina.Host
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * Tomcat的WebServerFactory
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class TomcatServletWebServerFactory(private var port: Int = 9966) : ServletWebServerFactory,
    ConfigurableWebServerFactory {

    companion object {

        /**
         * 默认的Tomcat的Protocol
         */
        const val DEFAULT_PROTOCOL = "org.apache.coyote.http11.Http11NioProtocol"
    }

    constructor() : this(9966)

    /**
     * protocol
     */
    private var protocol = DEFAULT_PROTOCOL

    /**
     * Tomcat的Connector的初始化器, 当Tomcat的Connector已经准备好时, 就会被自动去进行callback
     */
    private var connectorCustomizers = ArrayList<TomcatConnectorCustomizer>()

    /**
     * TomcatContext的Initializer, 当Tomcat的Context已经准备好时, 就会被自动去进行callback
     */
    private var contextInitializers = ArrayList<TomcatContextCustomizer>()

    /**
     * get tomcat port
     *
     * @return port
     */
    open fun getPort(): Int = this.port

    /**
     * set tomcat port
     *
     * @param port port
     */
    override fun setPort(port: Int) {
        this.port = port
    }

    /**
     * 设置Tomcat的Protocol
     *
     * @param protocol protocol
     */
    open fun setProtocol(protocol: String) {
        this.protocol = protocol
    }

    /**
     * 获取WebServer
     *
     * @return TomcatWebServer
     */
    override fun getWebServer(): TomcatWebServer {
        return getWebServer(initializers = emptyArray())
    }

    /**
     * 获取WebServer
     *
     * @param initializers 需要利用ServletContext去执行初始化的Initializer
     * @return TomcatWebServer
     */
    override fun getWebServer(vararg initializers: ServletContextInitializer): TomcatWebServer {
        val tomcat = Tomcat()

        val connector = Connector(this.protocol)
        connector.throwOnFailure = true
        tomcat.service.addConnector(connector)

        // 执行对于Connector的更多自定义
        customizeConnector(connector)
        tomcat.connector = connector
        tomcat.host.autoDeploy = false

        configureEngine(tomcat.engine)

        // 准备Context
        prepareContext(tomcat.host, arrayOf(*initializers))
        return getTomcatWebServer(tomcat)
    }

    protected open fun prepareContext(host: Host, initializer: Array<ServletContextInitializer>) {
        val context = TomcatEmbeddedContext()

        context.name = ""
        context.path = ""

        val docBase = createTempDir("tomcat-docbase")
        context.docBase = docBase.absolutePath

        // 这个LifecycleListener特别重要...没有它Tomcat启动不起来
        context.addLifecycleListener(Tomcat.FixContextListener())

        // add child to host
        host.addChild(context)

        // 配置Context
        configureContext(context, initializer)

        // 对Context去进行后置处理
        postProcessContext(context)
    }

    protected open fun configureContext(context: Context, initializers: Array<ServletContextInitializer>) {
        val tomcatStarter = TomcatStarter(initializers)
        if (context is TomcatEmbeddedContext) {
            context.setTomcatStarter(tomcatStarter)
        }
        // 添加ServletContainer的Initializer
        context.addServletContainerInitializer(tomcatStarter, emptySet())

        // 使用所有的ContextInitializer去对Context去进行自定义
        contextInitializers.forEach { it.customize(context) }
    }

    protected open fun postProcessContext(context: Context) {

    }

    /**
     * 执行对于Connector的自定义
     *
     * @param connector Connector
     */
    protected open fun customizeConnector(connector: Connector) {
        connector.port = getPort()
    }

    protected open fun configureEngine(engine: Engine) {

    }

    /**
     * 根据[Tomcat]去获取到[TomcatWebServer]
     *
     * @param tomcat Tomcat
     * @return TomcatWebServer
     */
    protected open fun getTomcatWebServer(tomcat: Tomcat): TomcatWebServer {
        return TomcatWebServer(tomcat)
    }

    protected fun createTempDir(prefix: String): File {
        return try {
            val tempDir = Files.createTempDirectory(prefix + "." + getPort() + ".").toFile()
            tempDir.deleteOnExit()
            tempDir
        } catch (ex: IOException) {
            throw IllegalStateException(
                "Unable to create tempDir. java.io.tmpdir is set to " + System.getProperty("java.io.tmpdir"), ex
            )
        }
    }
}