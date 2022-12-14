package com.wanna.framework.web.server.servlet

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.context.ConfigurableWebApplicationContext
import com.wanna.framework.web.context.ConfigurableWebEnvironment
import com.wanna.framework.web.context.WebApplicationContext
import com.wanna.framework.web.context.support.GenericWebApplicationContext
import com.wanna.framework.web.context.support.WebApplicationContextUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.Throws

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
abstract class FrameworkServlet : HttpServletBean(), ApplicationContextAware {

    /**
     * WebApplicationContext
     */
    @Nullable
    private var webApplicationContext: WebApplicationContext? = null

    /**
     * 从ServletContext当中去寻找WebApplicationContext的属性名, 可以为null
     */
    @Nullable
    private var contextAttribute: String? = null

    /**
     * 要用于去进行创建WebApplicationContext的类(必须是ConfigurableWebApplicationContext的子类)
     *
     * @see ConfigurableWebApplicationContext
     */
    private var contextClass: Class<*> = GenericWebApplicationContext::class.java

    /**
     * set ApplicationContext
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        // 只有目标的WebApplicationContext是一个WebApplicationContext, 我们才完成初始化
        if (applicationContext is WebApplicationContext) {
            this.webApplicationContext = applicationContext
        }
    }

    /**
     * 初始化ServletBean
     */
    override fun initServletBean() {
        // 初始化WebApplicationContext
        this.webApplicationContext = initWebApplicationContext()
    }

    /**
     * 初始化当前的Framework当中需要使用到的WebApplicationContext
     *
     * @return WebApplicationContext
     */
    protected open fun initWebApplicationContext(): WebApplicationContext {
        // 从ServletContext当中根据root的属性去寻找到rootContext
        val rootContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)

        // 1.优先使用setApplicationContext去设置的ApplicationContext, 去作为要使用的WebApplicationContext
        var wac: WebApplicationContext? = null
        if (this.webApplicationContext != null) {
            wac = this.webApplicationContext
            if (wac is ConfigurableWebApplicationContext) {

                // 如果之前WebApplicationContext没有被刷新的话, 那么需要对该ApplicationContext去进行刷新
                if (!wac.isActive()) {
                    if (wac.getParent() == null) {
                        wac.setParent(rootContext)
                    }

                    // 执行对于WebApplicationContext的配置和刷新
                    configureAndRefreshWebApplicationContext(wac)
                }
            }
        }

        // 2.接着, 尝试从ServletContext当中, 根据配置的contextAttribute去进行寻找到合适的WebApplicationContext
        if (wac == null) {
            wac = findWebApplicationContext()
        }

        // 3.如果还是不存在的话, 那么在这里去尝试去根据contextClass去创建一个新的WebApplicationContext
        if (wac == null) {
            wac = createWebApplicationContext(rootContext)
        }

        return wac
    }

    /**
     * 从ServletContext当中, 根据contextAttribute去寻找到合适的WebApplicationContext
     *
     * @return WebApplicationContext(如果没有配置contextAttribute, return null)
     * @throws IllegalStateException 配置了contextAttribute, 但是在ServletContext当中没有根据该属性名去找到合适的WebApplicationContext
     */
    @Throws(IllegalStateException::class)
    @Nullable
    protected open fun findWebApplicationContext(): WebApplicationContext? {
        val contextAttribute = getContextAttribute() ?: return null
        return WebApplicationContextUtils.getWebApplicationContext(servletContext, contextAttribute)
            ?: throw IllegalStateException("No WebApplicationContext found: initializer not registered?")
    }

    /**
     * 执行对于WebApplicationContext的配置和刷新
     *
     * @param wac WebApplicationContext
     */
    protected open fun configureAndRefreshWebApplicationContext(wac: ConfigurableWebApplicationContext) {

        // 初始化ServletConfig/ServletContext
        wac.setServletConfig(servletConfig)
        wac.setServletContext(servletContext)

        // 如果Environment是ConfigurableWebEnvironment, 那么根据ServletContext/ServletConfig去初始化PropertySources
        val environment = wac.getEnvironment()
        if (environment is ConfigurableWebEnvironment) {
            environment.initPropertySources(servletContext, servletConfig)
        }

        // 模板方法, 对于WebApplicationContext的后置处理, 交给子类去进行实现
        postProcessWebApplicationContext(wac)

        // 将ApplicationContextInitializer去应用给WebApplicationContext
        applyInitializers(wac)

        // 刷新WebApplicationContext
        wac.refresh()
    }

    /**
     * 针对给定的WebApplicationContext去应用ApplicationContextInitializer
     *
     * @param wac WebApplicationContext
     */
    protected open fun applyInitializers(wac: ConfigurableWebApplicationContext) {

    }

    /**
     * 模板方法, 对于WebApplicationContext的后置处理, 交给子类去进行实现
     *
     * @param wac WebApplicationContext
     */
    protected open fun postProcessWebApplicationContext(wac: ConfigurableWebApplicationContext) {

    }

    /**
     * 根据contextClass创建默认的WebApplicationContext
     *
     * @param parent parent WebApplicationContext(or null)
     * @return WebApplicationContext
     */
    protected open fun createWebApplicationContext(@Nullable parent: WebApplicationContext?): WebApplicationContext {
        val contextClass = getContextClass()

        // 检查类型是否是ConfigurableWebApplicationContext
        if (!ClassUtils.isAssignFrom(ConfigurableWebApplicationContext::class.java, contextClass)) {
            throw IllegalStateException("Given contextClass is ${ClassUtils.getQualifiedName(contextClass)} is not a ConfigurableWebApplicationContext, servletName=$servletName")
        }

        // 实例化WebApplicationContext
        val wac = BeanUtils.instantiateClass(contextClass) as ConfigurableWebApplicationContext
        if (parent != null) {
            wac.setParent(parent)
        }

        // set Environment
        wac.setEnvironment(getEnvironment())

        // 刷新新创建的WebApplicationContext
        configureAndRefreshWebApplicationContext(wac)
        return wac
    }

    /**
     * 去执行真正地处理请求
     *
     * @param request request
     * @param response response
     */
    protected open fun processRequest(request: HttpServletRequest, response: HttpServletResponse) {
        doService(request, response)
    }

    protected abstract fun doService(request: HttpServletRequest, response: HttpServletResponse)


    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        processRequest(req, resp)
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        processRequest(req, resp)
    }

    override fun doHead(req: HttpServletRequest, resp: HttpServletResponse) {
        processRequest(req, resp)
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        processRequest(req, resp)
    }

    override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
        processRequest(req, resp)
    }

    override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
        processRequest(req, resp)
    }

    override fun doOptions(req: HttpServletRequest, resp: HttpServletResponse) {
        processRequest(req, resp)
    }

    override fun doTrace(req: HttpServletRequest, resp: HttpServletResponse) {
        processRequest(req, resp)
    }

    /**
     * 设置要从ServletContext当中去寻找WebApplicationContext的属性名, 可以为null
     *
     * @param contextAttribute 属性名(or null)
     */
    open fun setContextAttribute(@Nullable contextAttribute: String?) {
        this.contextAttribute = contextAttribute
    }

    /**
     * 设置要从ServletContext当中去寻找WebApplicationContext的属性名, 可以为null
     *
     * @return contextAttribute属性名(or null)
     */
    @Nullable
    open fun getContextAttribute(): String? = this.contextAttribute

    /**
     * 设置用于去创建WebApplicationContext的类(必须是ConfigurableWebApplicationContext的子类)
     *
     * @param contextClass contextClass
     * @see ConfigurableWebApplicationContext
     */
    open fun setContextClass(contextClass: Class<*>) {
        this.contextClass = contextClass
    }

    /**
     * 获取到用于去创建WebApplicationContext的类
     *
     * @return contextClass
     */
    open fun getContextClass(): Class<*> = this.contextClass
}