package com.wanna.framework.web.method.support

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.context.request.NativeWebRequest

/**
 * 它聚合了HandlerMethod的ReturnValueHandler列表，遍历所有的ReturnValueHandler，去完成ReturnValue的解析
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

    override fun supportsReturnType(parameter: MethodParameter): Boolean {
        returnValueHandlers.forEach {
            if (it.supportsReturnType(parameter)) {
                return true
            }
        }
        return false
    }

    /**
     * 从ReturnValueHandler列表当中，去选出合适的Handler，来处理给定的返回值
     *
     * @param returnValue 返回值
     * @param webRequest NativeWebRequest(request and response)
     * @param returnType 返回值类型
     * @return 如果找到了合适的Handler去进行处理，return Handler处理结果
     * @throws IllegalArgumentException 如果没有找到合适的Handler去处理
     */
    override fun handleReturnValue(returnValue: Any?, webRequest: NativeWebRequest, returnType: MethodParameter) {
        val selectHandler = selectHandler(returnValue, webRequest, returnType)
            ?: throw IllegalArgumentException("未知类型的返回值，没有找到合适的ReturnValueHandler去进行处理")

        // 使用选择出来的Handler去进行返回值的处理工作
        selectHandler.handleReturnValue(returnValue, webRequest, returnType)
    }

    /**
     * 选择出合适的ReturnValueHandler去处理返回值
     *
     * @param returnValue 执行HandlerMethod的返回值
     * @param webRequest NativeWebRequest(request and response)
     * @param returnType 返回值类型
     * @return 如果找到了合适的Handler去进行处理，return Handler；不然return null
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