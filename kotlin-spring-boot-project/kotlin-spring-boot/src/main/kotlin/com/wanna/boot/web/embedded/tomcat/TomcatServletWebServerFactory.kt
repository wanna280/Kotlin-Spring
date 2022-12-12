package com.wanna.boot.web.embedded.tomcat

import com.wanna.boot.web.server.ConfigurableWebServerFactory
import com.wanna.boot.web.servlet.ServletContextInitializer
import com.wanna.boot.web.servlet.ServletWebServerFactory
import com.wanna.framework.context.ResourceLoaderAware
import com.wanna.framework.core.io.ResourceLoader
import org.apache.catalina.Context
import org.apache.catalina.Engine
import org.apache.catalina.Host
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import org.apache.coyote.ProtocolHandler
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files

/**
 * Tomcat的WebServerFactory
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class TomcatServletWebServerFactory(private var port: Int = 9966) : ServletWebServerFactory,
    ConfigurableWebServerFactory, ResourceLoaderAware {

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
     * contextPath, use "", rather than "/"
     */
    private var contextPath: String = ""

    /**
     * Tomcat的baseDirectory, 也就是CatalinaHome/CatalinaBase,
     * 默认情况下, 会从系统属性的"catalina.base"/"catalina.home"去进行寻找,
     * 如果无法寻找到的话, 将会采用"{user.dir}/tomcat.{port}"去作为Tomcat的baseDir
     */
    private var baseDirectory: File? = null

    /**
     * encoding
     */
    private var uriEncoding: Charset = Charsets.UTF_8

    /**
     * ResourceLoader
     */
    private var resourceLoader: ResourceLoader? = null

    /**
     * Tomcat的Connector的自定义化器, 当Tomcat的Connector已经准备好时, 就会被自动去进行callback
     */
    private var connectorCustomizers = ArrayList<TomcatConnectorCustomizer>()

    /**
     * Tomcat的Context的自定义化器, 当Tomcat的Context已经准备好时, 就会被自动去进行callback
     */
    private var contextCustomizers = ArrayList<TomcatContextCustomizer>()

    /**
     * Tomcat的ProtocolHandler的自定义化器, 当Tomcat的ProtocolHandler已经准备好时, 就会自动去进行callback
     */
    private var protocolHandlerCustomizers = ArrayList<TomcatProtocolHandlerCustomizer<ProtocolHandler>>()

    /**
     * 一些需要去进行额外添加的Tomcat当中的Connector
     */
    private val additionalTomcatConnectors = ArrayList<Connector>()

    /**
     * 获取TomcatWebServer
     *
     * @return TomcatWebServer
     */
    override fun getWebServer(): TomcatWebServer {
        return getWebServer(initializers = emptyArray())
    }

    /**
     * 获取TomcatWebServer, 并完成TomcatWebSever的初始化工作
     *
     * @param initializers 需要利用ServletContext去执行初始化的Initializer
     * @return 创建出来的TomcatWebServer
     */
    override fun getWebServer(vararg initializers: ServletContextInitializer): TomcatWebServer {
        val tomcat = Tomcat()

        // set Tomcat BaseDir(CatalinaBase/CatalinaHome)
        val baseDir = this.baseDirectory ?: createTempDir("tomcat")
        tomcat.setBaseDir(baseDir.absolutePath)

        // 正常的关系是Tomcat->Server->Services->Connectors, 但是因为绝大多数情况下,
        // 对于Service/Connector很可能只用到一个, 因此嵌入式的Tomcat提供了快速去构建Service/Connector的方式...
        val connector = Connector(this.protocol)
        connector.throwOnFailure = true

        // Note: 如果自定义Connector的话, 那么需要将它去同时添加到Service/Tomcat当中...
        tomcat.service.addConnector(connector)
        tomcat.connector = connector
        tomcat.host.autoDeploy = false

        // 执行对于Connector的更多自定义
        customizeConnector(connector)

        // 执行对于Tomcat的Engine的自定义
        configureEngine(tomcat.engine)

        // 将额外的TomcatConnector, 全部添加到Tomcat的Service当中
        getAdditionalTomcatConnectors().forEach(tomcat.service::addConnector)

        // 准备Context
        prepareContext(tomcat.host, arrayOf(*initializers))

        // 根据Tomcat, 获取到TomcatWebServer...
        return getTomcatWebServer(tomcat)
    }

    /**
     * 准备Context, 构建处理一个Tomcat的Context, 并把它添加到Host当中
     *
     * @param host Tomcat Host
     * @param initializers ServletContext的初始化器列表
     */
    protected open fun prepareContext(host: Host, initializers: Array<ServletContextInitializer>) {
        val context = TomcatEmbeddedContext()

        // name必须设置(不然会触发NPE), 并且不能以"/"开头(不然会触发IllegalArgumentException)
        context.name = getContextPath()
        context.path = getContextPath()

        // must create temp dir
        // which is used by Tomcat to identify the location of the web application and to serve its contents to clients.
        val docBase = createTempDir("tomcat-docbase")
        context.docBase = docBase.absolutePath

        // 这个LifecycleListener特别重要...没有它Tomcat启动不起来
        // 这个Listener在不使用"web.xml"的情况下是必须添加的Listener
        context.addLifecycleListener(Tomcat.FixContextListener())

        // add child to host
        host.addChild(context)

        // 执行更多的自定义Context
        configureContext(context, initializers)

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
        contextCustomizers.forEach { it.customize(context) }
    }

    protected open fun postProcessContext(context: Context) {

    }

    /**
     * 执行对于Tomcat的Connector的自定义
     *
     * @param connector Connector
     */
    protected open fun customizeConnector(connector: Connector) {
        connector.port = getPort()

        // 利用ProtocolHandler的Customizer去对Connector当中的ProtocolHandler去进行自定义的处理
        protocolHandlerCustomizers.forEach { it.customize(connector.protocolHandler) }

        // uriEncoding
        connector.uriEncoding = getUriEncoding().name()

        // 利用所有的ConnectorCustomizer, 去对Connector去执行初始化...
        connectorCustomizers.forEach { it.customize(connector) }
    }

    /**
     * 执行对于Tomcat的Engine的自定义
     *
     * @param engine Engine
     */
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

    /**
     * 创建一个临时的文件夹
     *
     * @param prefix 文件夹名的前缀
     * @return 临时文件加对应的File
     */
    protected open fun createTempDir(prefix: String): File {
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

    /**
     * get tomcat connector port
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
     * set ContextPath
     *
     * @param contextPath contextPath
     */
    open fun setContextPath(contextPath: String) {
        this.contextPath = contextPath
    }

    /**
     * get ContextPath
     *
     * @return contextPath
     */
    open fun getContextPath(): String = this.contextPath

    /**
     * setEncoding
     *
     * @param uriEncoding encoding
     */
    open fun setUriEncoding(uriEncoding: Charset) {
        this.uriEncoding = uriEncoding
    }

    /**
     * getEncoding
     *
     * @return encoding
     */
    open fun getUriEncoding(): Charset = this.uriEncoding

    /**
     * 添加额外的Tomcat的Connector
     *
     * @param connectors 需要额外添加的Tomcat的Connector
     */
    open fun addAdditionalTomcatConnectors(vararg connectors: Connector) {
        this.additionalTomcatConnectors += arrayListOf(*connectors)
    }

    /**
     * 获取需要去进行额外添加的Tomcat的Connector
     *
     * @return Connectors
     */
    open fun getAdditionalTomcatConnectors(): List<Connector> = this.additionalTomcatConnectors

    /**
     * set ResourceLoader
     *
     * @param resourceLoader ResourceLoader
     */
    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }

    /**
     * set Tomcat Base Directory
     *
     * @param baseDirectory baseDirectory
     */
    open fun setBaseDirectory(baseDirectory: File) {
        this.baseDirectory = baseDirectory
    }
}