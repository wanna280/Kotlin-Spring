package com.wanna.framework.web.method.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.method.HandlerMethod
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * 这是一个可以被执行的HandlerMethod，提供了invokeAndHandle方法，外部可以直接调用，去完成方法的调用
 *
 * @see invokeAndHandle
 */
open class InvocableHandlerMethod : HandlerMethod() {

    companion object {
        private val logger = LoggerFactory.getLogger(InvocableHandlerMethod::class.java)

        // 空参数的常量
        private val EMPTY_ARGS = emptyArray<Any?>()

        @JvmStatic
        fun newInvocableHandlerMethod(handlerMethod: HandlerMethod, handler: Any): InvocableHandlerMethod {
            return HandlerMethodUtil.newHandlerMethod(handlerMethod, handler, InvocableHandlerMethod::class.java)
        }

        @JvmStatic
        fun newInvocableHandlerMethod(handlerMethod: HandlerMethod): InvocableHandlerMethod {
            return HandlerMethodUtil.newHandlerMethod(handlerMethod, InvocableHandlerMethod::class.java)
        }

        @JvmStatic
        fun newInvocableHandlerMethod(
            beanFactory: BeanFactory, beanName: String, method: Method
        ): InvocableHandlerMethod {
            return HandlerMethodUtil.newHandlerMethod(beanFactory, beanName, method, InvocableHandlerMethod::class.java)
        }

        @JvmStatic
        fun newInvocableHandlerMethod(bean: Any, method: Method): InvocableHandlerMethod {
            return HandlerMethodUtil.newHandlerMethod(bean, method, InvocableHandlerMethod::class.java)
        }
    }


    // 参数名发现器
    var parameterNameDiscoverer = DefaultParameterNameDiscoverer()

    // 参数解析器列表
    var argumentResolvers: HandlerMethodArgumentResolverComposite? = HandlerMethodArgumentResolverComposite()

    // 返回值解析器列表
    var returnValueHandlers: HandlerMethodReturnValueHandlerComposite? = HandlerMethodReturnValueHandlerComposite()

    /**
     * 执行目标方法，并处理目标方法的返回值
     *
     * @param webRequest NativeWebRequest(request and response)
     */
    open fun invokeAndHandle(webRequest: ServerWebRequest, mavContainer: ModelAndViewContainer) {
        // 遍历所有的ArgumentResolver去解析方法参数，并反射执行目标方法
        val returnValue = invokeForRequest(webRequest, mavContainer)

        // 遍历所有的ReturnValueHandler，尝试去处理方法的返回值
        try {
            this.returnValueHandlers?.handleReturnValue(
                returnValue, webRequest, getReturnValueType(returnValue), mavContainer
            )
        } catch (ex: Exception) {
            if (logger.isTraceEnabled) {
                logger.trace("处理方法的返回值失败，原因是[${ex.message}]")
            }
            throw ex
        }
    }


    /**
     * 获取HandlerMethod执行时，应该用到的参数值列表
     *
     * @param webRequest NativeWebRequest(request and response)
     * @return 当前HandlerMethod的参数列表params
     * @throws IllegalStateException 如果没有找到合适的方法的参数解析器去解析当前的方法参数
     * @throws Exception 如果使用参数解析器解析过程当中发生了异常
     */
    protected open fun getMethodArgumentValues(
        webRequest: ServerWebRequest, mavContainer: ModelAndViewContainer?
    ): Array<Any?> {
        val methodParameters = parameters!!
        if (methodParameters.isEmpty()) {
            return EMPTY_ARGS
        }
        val params = arrayOfNulls<Any?>(methodParameters.size)
        val resolvers = argumentResolvers
        methodParameters.indices.forEach {
            val parameter = methodParameters[it]
            // 在解析之前，需要先初始化参数名发现器，方便在解析参数的值时，可以去获取参数的name
            parameter.initParameterNameDiscoverer(this.parameterNameDiscoverer)
            // 解析方法参数
            if (resolvers != null) {
                if (!resolvers.supportsParameter(parameter)) {
                    throw IllegalStateException("解析方法参数[$parameter]失败，原因是没有找到合适的参数解析器去进行解析")
                }
                try {
                    params[it] = resolvers.resolveArgument(parameter, webRequest, mavContainer)
                } catch (ex: Exception) {
                    if (logger.isDebugEnabled) {
                        logger.debug("使用参数解析器去解析参数[$parameter]失败，原因是[${ex.message}]")
                    }
                    throw ex
                }
            }
        }
        return params
    }

    /**
     * 解析HandlerMethod方法参数，并执行目标方法
     *
     * @param webRequest NativeWebRequest(request and response)
     * @return Handler方法的返回值
     */
    open fun invokeForRequest(webRequest: ServerWebRequest, mavContainer: ModelAndViewContainer): Any? {
        val args = getMethodArgumentValues(webRequest, mavContainer)
        return doInvoke(*args)
    }

    /**
     * 使用反射的方式去执行目标方法
     *
     * @param args 目标handlerMethod的参数列表
     * @return handlerMethod的返回值
     */
    protected open fun doInvoke(vararg args: Any?): Any? {
        val method = method!!
        ReflectionUtils.makeAccessiable(method)
        try {
            return method.invoke(this.bean, *args)
        } catch (ex: IllegalArgumentException) {
            throw IllegalArgumentException("执行HandlerMethod出现了不合法的方法参数，[method=${this.method}, args=${args.contentToString()}]")
        } catch (ex: InvocationTargetException) {
            throw IllegalStateException("执行目标方法发生错误，原因是-->${ex.targetException}")
        }
    }
}