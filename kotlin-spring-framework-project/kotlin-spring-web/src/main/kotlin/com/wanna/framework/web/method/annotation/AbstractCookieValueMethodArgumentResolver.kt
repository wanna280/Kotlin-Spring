package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.bind.ServerRequestBindingException
import com.wanna.framework.web.bind.annotation.CookieValue

/**
 * 为[CookieValue]的实现提供抽象的实现, 提供对于`@CookieValue`注解的解析工作
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/22
 * @see CookieValue
 */
abstract class AbstractCookieValueMethodArgumentResolver : AbstractNamedValueMethodArgumentResolver() {

    /**
     * 针对`@CookieValue`注解去创建出来对应的NamedValueInfo
     *
     * @param parameter 标注有`@CookieValue`注解的方法参数
     * @return 针对`@CookieValue`注解去解析得到的NamedValueInfo
     */
    override fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        val cookieValue = parameter.getAnnotation(CookieValue::class.java)!!
        val cookieName = cookieValue.value.ifBlank { cookieValue.name }
        return CookieValueNamedValueInfo(cookieName, cookieValue.required, cookieValue.defaultValue)
    }

    /**
     * 支持去处理方法参数上标注了[CookieValue]注解的那些方法参数
     *
     * @param parameter 方法参数
     * @return 如果方法参数上有[CookieValue]的话, return true; 否则return false
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.getAnnotation(CookieValue::class.java) != null

    /**
     * 处理required=true, 但是不存在有这样的Cookie值的情况
     *
     * @param name cookieName
     * @param parameter 方法参数
     */
    override fun handleMissingValue(name: String, parameter: MethodParameter) {
        throw ServerRequestBindingException("在绑定[${parameter.getExecutable()}]的[${parameter.getParameter()}]时遇到了, 缺失[$name]对应的HttpCookie")
    }

    /**
     * 维护了一个`@CookieValue`注解当中的相关信息
     *
     * @param name cookieName
     * @param required 该Cookie值是否必须是存在的?
     * @param defaultValue 如果不存在该cookieName的Cookie, 那么CookieValue的默认值要采用什么
     */
    private class CookieValueNamedValueInfo(name: String, required: Boolean, defaultValue: String) :
        NamedValueInfo(name, required, defaultValue)
}