package com.wanna.framework.web.method.support

import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
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
        // Logger
        private val logger = LoggerFactory.getLogger(InvocableHandlerMethod::class.java)

        // 空参数的常量
        private val EMPTY_ARGS = emptyArray<Any?>()

        @JvmStatic
        fun newInvocableHandlerMethod(handlerMethod: HandlerMethod): InvocableHandlerMethod {
            return HandlerMethodUtil.newHandlerMethod(handlerMethod, InvocableHandlerMethod::class.java)
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

    // BinderFactory(提供了参数类型的转换等功能)
    var binderFactory: WebDataBinderFactory? = null

    /**
     * 执行目标HandlerMethod，并处理目标方法的返回值
     *
     * @param webRequest NativeWebRequest(request and response)
     * @param mavContainer ModelAndView容器
     * @param provideArgs 外部提供的参数列表，在进行参数解析时，优先从给定的参数列表当中获取
     */
    open fun invokeAndHandle(
        webRequest: ServerWebRequest, mavContainer: ModelAndViewContainer, vararg provideArgs: Any
    ) {
        // 遍历所有的ArgumentResolver去解析方法参数，并反射执行目标方法
        val returnValue = invokeForRequest(webRequest, mavContainer, *provideArgs)
        val returnValueType = getReturnValueType(returnValue)

        // 遍历所有的ReturnValueHandler，去找到合适的一个去进行方法的返回值的处理...
        try {
            this.returnValueHandlers?.handleReturnValue(returnValue, webRequest, returnValueType, mavContainer)
        } catch (ex: Exception) {
            if (logger.isTraceEnabled) {
                logger.trace("处理方法的返回值失败，原因是[${ex.message}]")
            }
            throw ex
        }
    }


    /**
     * 使用HandlerMethod当中组合的参数解析器去获取HandlerMethod执行时，应该用到的参数值列表
     *
     * @param webRequest NativeWebRequest(request and response)
     * @param mavContainer ModelAndViewContainer
     * @param provideArgs 外部提供的参数列表，在进行参数解析时，优先从给定的参数列表当中获取
     * @return 当前HandlerMethod的参数列表params
     * @throws IllegalStateException 如果没有找到合适的方法的参数解析器去解析当前的方法参数
     * @throws Exception 如果使用参数解析器解析过程当中发生了异常
     */
    protected open fun getMethodArgumentValues(
        webRequest: NativeWebRequest, mavContainer: ModelAndViewContainer?, vararg provideArgs: Any
    ): Array<Any?> {
        val methodParameters = parameters ?: throw IllegalStateException("HandlerMethod的参数列表不能为null")
        if (methodParameters.isEmpty()) {
            return EMPTY_ARGS
        }
        val params = arrayOfNulls<Any?>(methodParameters.size)
        val resolvers = argumentResolvers
        (methodParameters.indices).forEach {
            val parameter = methodParameters[it]
            // 先尝试从外部提供的参数列表当中去进行寻找类型匹配的参数(比如@ExceptionHandler需要用到具体的异常信息，就支持从这里去进行给定)
            params[it] = findProvidedArgument(parameter, *provideArgs)
            // 如果找到了，那么就不使用参数解析器去进行匹配了；如果没有找到，那么就得交给参数解析器去完成解析了
            if (params[it] != null) {
                return@forEach
            }
            // 在解析之前，需要先初始化参数名发现器，方便在解析参数的值时，可以去获取参数的name
            parameter.initParameterNameDiscoverer(this.parameterNameDiscoverer)
            // 交给方法参数解析器列表，挨个尝试去解析方法参数
            if (resolvers != null) {
                if (!resolvers.supportsParameter(parameter)) {
                    throw IllegalStateException("解析方法参数[$parameter]失败，原因是没有找到合适的参数解析器去进行解析")
                }
                try {
                    params[it] = resolvers.resolveArgument(parameter, webRequest, mavContainer, binderFactory)
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
     * 从提供的参数当中，找到类型匹配的参数
     *
     * @param parameter 目标参数信息
     * @param provideArgs 外部提供的参数列表，在进行参数解析时，优先从给定的参数列表当中获取
     * @return 如果从候选的参数列表当中找到了合适的参数，那么return该参数；如果没有匹配的，return null
     */
    protected open fun findProvidedArgument(parameter: MethodParameter, vararg provideArgs: Any): Any? {
        provideArgs.forEach {
            if (parameter.getParameterType().isInstance(it)) {
                return it
            }
        }
        return null
    }

    /**
     * 解析HandlerMethod方法参数，并执行目标方法
     *
     * @param webRequest NativeWebRequest(request and response)
     * @param mavContainer ModelAndView容器
     * @param provideArgs 外部提供的参数列表，在进行参数解析时，优先从给定的参数列表当中获取
     * @return 执行目标Handler方法的返回值
     */
    open fun invokeForRequest(
        webRequest: NativeWebRequest, mavContainer: ModelAndViewContainer?, vararg provideArgs: Any
    ): Any? {
        val args = getMethodArgumentValues(webRequest, mavContainer, *provideArgs)
        if (logger.isTraceEnabled) {
            logger.trace("方法参数列表为--->[${args.contentToString()}]")
        }
        return doInvoke(*args)
    }

    /**
     * 使用反射的方式去执行目标方法
     *
     * @param args 目标handlerMethod的参数列表
     * @return handlerMethod的执行结果的返回值
     */
    protected open fun doInvoke(vararg args: Any?): Any? {
        val method = method ?: throw IllegalStateException("HandlerMethod当中方法为null")
        ReflectionUtils.makeAccessiable(method)
        try {
            return ReflectionUtils.invokeMethod(method, this.bean, *args)
        } catch (ex: IllegalArgumentException) {
            throw IllegalArgumentException(
                "执行HandlerMethod出现了不合法参数，[method=${method}, args=${args.contentToString()}]",
                ex
            )
        } catch (ex: InvocationTargetException) {
            throw IllegalStateException("执行目标方法发生错误，原因是-->${ex.targetException}", ex)
        }
    }
}