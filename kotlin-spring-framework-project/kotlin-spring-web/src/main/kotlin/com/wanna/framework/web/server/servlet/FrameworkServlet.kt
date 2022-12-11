package com.wanna.framework.web.server.servlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
abstract class FrameworkServlet : HttpServletBean() {

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
}