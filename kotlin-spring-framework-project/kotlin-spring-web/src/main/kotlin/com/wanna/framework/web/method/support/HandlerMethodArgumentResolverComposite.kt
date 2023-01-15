package com.wanna.framework.web.method.support

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import java.util.concurrent.ConcurrentHashMap

/**
 * 它内部聚合了多个HandlerMethodArgumentResolver, 去协作完成请求参数的处理;
 * 它内部支持了参数解析器的缓存, 在判断完是否支持解析之后; 立刻需要拿出该处理器去进行参数的解析, 因此可以做一遍缓存
 *
 * @see argumentResolverCache
 */
class HandlerMethodArgumentResolverComposite : HandlerMethodArgumentResolver {

    private val argumentResolvers = ArrayList<HandlerMethodArgumentResolver>()

    // 参数解析器缓存
    private val argumentResolverCache = ConcurrentHashMap<MethodParameter, HandlerMethodArgumentResolver>()

    fun addArgumentResolvers(vararg resolvers: HandlerMethodArgumentResolver): HandlerMethodArgumentResolverComposite {
        this.argumentResolvers += arrayListOf(*resolvers)
        return this
    }

    fun addArgumentResolvers(resolvers: Collection<HandlerMethodArgumentResolver>): HandlerMethodArgumentResolverComposite {
        this.argumentResolvers += resolvers
        return this
    }

    /**
     * 是否支持处理这样的参数？遍历所有的ArgumentResolver, 只要其中一个能处理就行
     *
     * @param parameter 参数解析器
     * @return 能否支持解析？只要找到了合适的ArgumentResolver, 那么return true; 不然return false
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return getArgumentResolver(parameter) != null
    }


    /**
     * @param parameter 方法参数
     * @param webRequest NativeWebRequest(request and response)
     * @return 使用ArgumentResolver解析到的方法参数
     * @throws IllegalArgumentException 如果没有找到合适的ArgumentResolver去处理该类型的参数
     */
    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val resolver = getArgumentResolver(parameter)
        if (resolver != null) {
            return resolver.resolveArgument(parameter, webRequest, mavContainer, binderFactory)
        }
        throw IllegalArgumentException("不支持的参数类型[$parameter], 没有找到合适的ArgumentResolver去进行解析该类型的参数")
    }

    /**
     * 获取可以用来处理目标方法参数的参数解析器
     *
     * @param parameter 方法参数
     * @return 如果找到合适的参数解析器, 那么return 该参数解析器; 如果没有找到合适的, return null
     */
    private fun getArgumentResolver(parameter: MethodParameter): HandlerMethodArgumentResolver? {
        var resolver = argumentResolverCache[parameter]
        if (resolver == null) {
            argumentResolvers.forEach {
                if (it.supportsParameter(parameter)) {
                    resolver = it
                    argumentResolverCache[parameter] = resolver!!
                    return it
                }
            }
        }
        return resolver
    }
}