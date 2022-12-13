package com.wanna.framework.web.server.servlet

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.lang.Nullable
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
     * 初始化WebApplicationContext
     *
     * @return WebApplicationContext
     */
    protected open fun initWebApplicationContext(): WebApplicationContext {
        // 从ServletContext当中根据root的属性去寻找到rootContext
        val rootContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)


        // 1.优先使用setApplicationContext去设置ApplicationContext, 去作为要使用的WebApplicationContext
        var wac: WebApplicationContext? = null
        if (this.webApplicationContext != null) {
            wac = this.webApplicationContext
            if (wac is ConfigurableApplicationContext) {
                wac.setParent(rootContext)
            }
        }

        // 2.接着, 尝试从ServletContext当中, 根据配置的contextAttribute去进行寻找到合适的WebApplicationContext
        if (wac == null) {
            wac = findWebApplicationContext()
        }

        // 3.如果还是不存在的话, 那么在这里去创建一个WebApplicationContext
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
     * 创建默认的WebApplicationContext
     *
     * @param parent parent WebApplicationContext
     * @return WebApplicationContext
     */
    protected open fun createWebApplicationContext(parent: WebApplicationContext?): WebApplicationContext {
        val applicationContext = GenericWebApplicationContext()
        if (parent != null) {
            applicationContext.setParent(parent)
        }
        return applicationContext
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
     * @param contextAttribute 属性名
     */
    open fun setContextAttribute(@Nullable contextAttribute: String?) {
        this.contextAttribute = contextAttribute
    }

    /**
     * 设置要从ServletContext当中去寻找WebApplicationContext的属性名, 可以为null
     *
     * @return contextAttribute属性名(可能为null)
     */
    @Nullable
    open fun getContextAttribute(): String? = this.contextAttribute
}