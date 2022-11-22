package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.Cookie
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.bind.annotation.CookieValue

/**
 * 提供[CookieValue]注解的解析的方法参数解析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/22
 * @see CookieValue
 */
open class ServerCookieValueMethodArgumentResolver : AbstractCookieValueMethodArgumentResolver() {

    /**
     * 根据cookieName, 从request当中去解析出来对应的Cookie值
     *
     * @param name cookieName
     * @param parameter 标注了`@CookieValue`注解的方法参数
     * @param webRequest NativeWebRequest
     * @return 根据cookieName解析到的Cookie对象, 支持参数为Cookie类型, 当然也支持为字符串等类型
     */
    override fun resolveName(name: String, parameter: MethodParameter, webRequest: NativeWebRequest): Any? {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        val cookie = request.getCookies().firstOrNull { it.name == name }
        if (ClassUtils.isAssignFrom(Cookie::class.java, parameter.getParameterType())) {
            return cookie
        } else {
            return cookie?.value
        }
    }
}