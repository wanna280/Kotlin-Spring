package com.wanna.framework.web.server.servlet

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.DispatcherHandlerImpl
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * DispatcherHandler, 处理请求的派发, 将Servlet请求交给[DispatcherHandler]去进行真正的处理
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class DispatcherServlet : FrameworkServlet() {

    /**
     * ServletTransformer
     */
    private val servletTransformer = ServletTransformer()

    /**
     * DispatcherHandler
     */
    private var dispatcherHandler: DispatcherHandler = DispatcherHandlerImpl()


    /**
     * 设置DispatcherHandler
     *
     * @param dispatcherHandler DispatcherHandler
     */
    open fun setDispatcherHandler(dispatcherHandler: DispatcherHandler) {
        this.dispatcherHandler = dispatcherHandler
    }

    /**
     * 执行真正地处理请求
     *
     * @param request servlet request
     * @param response servlet response
     */
    override fun doService(request: HttpServletRequest, response: HttpServletResponse) {
        val httpServerRequest = servletTransformer.transform(request)
        val httpServerResponse = servletTransformer.transform(response)

        // do dispatch
        doDispatch(httpServerRequest, httpServerResponse)
    }

    protected open fun doDispatch(request: HttpServerRequest, response: HttpServerResponse) {
        // 使用DispatcherHandler去处理请求...
        dispatcherHandler.doDispatch(request, response)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        super.setApplicationContext(applicationContext)

        try {
            // init DispatcherHandler
            this.dispatcherHandler = applicationContext.getBean(DispatcherHandler::class.java)
        } catch (ex: NoSuchBeanDefinitionException) {
            // ignore
            logger.warn("cannot find DispatcherHandler from given ApplicationContext", ex)
        }
    }
}