package com.wanna.framework.web.method.support

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.context.request.NativeWebRequest

/**
 * 它聚合了HandlerMethod的ReturnValueHandler列表, 遍历所有的ReturnValueHandler, 去完成ReturnValue的解析
 *
 * @see HandlerMethodReturnValueHandler
 * @see supportsReturnType
 * @see handleReturnValue
 */
open class HandlerMethodReturnValueHandlerComposite : HandlerMethodReturnValueHandler {
    private val returnValueHandlers = ArrayList<HandlerMethodReturnValueHandler>()

    open fun addReturnValueHandlers(vararg returnValueHandlers: HandlerMethodReturnValueHandler): HandlerMethodReturnValueHandlerComposite {
        this.returnValueHandlers += arrayListOf(*returnValueHandlers)
        return this
    }

    open fun addReturnValueHandlers(handlers: Collection<HandlerMethodReturnValueHandler>): HandlerMethodReturnValueHandlerComposite {
        this.returnValueHandlers += handlers
        return this
    }

    /**
     * 是否支持处理该类型的返回值类型? 只要有其中一个ReturnValueHandler支持去匹配, 那么就支持去进行处理
     *
     * @param parameter 返回值类型封装成为的MethodParameter
     * @return 只要有其中一个ReturnValueHandler支持去处理, 那么return true
     */
    override fun supportsReturnType(parameter: MethodParameter) =
        this.returnValueHandlers.stream().anyMatch { it.supportsReturnType(parameter) }

    /**
     * 从内部组合的ReturnValueHandler列表当中, 去选出合适的ReturnValueHandler, 来处理给定的返回值结果
     *
     * @param returnValue 返回值结果
     * @param webRequest NativeWebRequest(request and response)
     * @param returnType 返回值类型封装成为的MethodParameter
     * @return 如果找到了合适的Handler去进行处理, return Handler处理结果
     * @throws IllegalArgumentException 如果没有找到合适的Handler去处理
     */
    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        val selectHandler = selectHandler(returnValue, webRequest, returnType)
            ?: throw IllegalArgumentException("未知类型的返回值, 没有找到合适的ReturnValueHandler去进行处理")

        // 使用选择出来的Handler去进行返回值的处理工作
        selectHandler.handleReturnValue(returnValue, webRequest, returnType, mavContainer)
    }

    /**
     * 选择出合适的ReturnValueHandler去处理返回值
     *
     * @param returnValue 执行HandlerMethod的返回值
     * @param webRequest NativeWebRequest(request and response)
     * @param returnType 返回值类型封装成为的MethodParameter
     * @return 如果找到了合适的Handler去进行处理, return Handler; 不然return null
     */
    private fun selectHandler(
        returnValue: Any?, webRequest: NativeWebRequest, returnType: MethodParameter
    ): HandlerMethodReturnValueHandler? {
        returnValueHandlers.forEach {
            if (it.supportsReturnType(returnType)) {
                return it
            }
        }
        return null
    }
}