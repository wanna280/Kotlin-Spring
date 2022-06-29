package com.wanna.boot.actuate.endpoint.web.mvc

import com.wanna.boot.actuate.endpoint.InvocationContext
import com.wanna.boot.actuate.endpoint.web.EndpointMapping
import com.wanna.boot.actuate.endpoint.web.ExposableWebEndpoint
import com.wanna.boot.actuate.endpoint.web.WebOperation
import com.wanna.boot.actuate.endpoint.web.WebOperationRequestPredicate
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.method.RequestMappingInfo
import com.wanna.framework.web.method.RequestMappingInfoHandlerMapping
import com.wanna.framework.web.method.annotation.RequestBody
import com.wanna.framework.web.method.annotation.ResponseBody
import com.wanna.framework.web.server.HttpServerRequest
import java.lang.reflect.Method

/**
 * 处理WebMvc的Endpoint的HandlerMapping，支持将Endpoint当中的Operation方法交给OperationHandler去进行代为调用；
 *
 * 我们沿用父类的大多数方法，包括路径的匹配，以及Mapping也使用RequestMappingInfo，甚至是
 * 管理Handler方法的MappingRegistry都沿用父类的模板方法，我们需要重写的，只是寻找HandlerMethod的方式罢了
 *
 * @see RequestMappingInfoHandlerMapping
 * @see RequestMappingInfo
 * @see OperationHandler
 *
 * @param endpoints  当前HandlerMapping当中已经注册的所有的Endpoint列表
 * @param endpointMapping Endpoint的Mapping
 */
abstract class AbstractWebMvcEndpointHandlerMapping(
    private val endpoints: List<ExposableWebEndpoint>,
    private val endpointMapping: EndpointMapping
) : RequestMappingInfoHandlerMapping() {

    // 处理请求的Handler方法(指向Operation的handle方法)
    private val handleMethod =
        ReflectionUtils.findMethod(
            OperationHandler::class.java,
            "handle",
            HttpServerRequest::class.java,
            Map::class.java
        )!!

    /**
     * 初始化HandlerMapping，将所有Endpoint当中的所有的Operation方法全部封装成为RequestMappingInfo，
     * 并将该"RequestMappingInfo-->HandlerMethod"映射关系，都去自动去注册到MappingRegistry当中
     */
    override fun initHandlerMethods() {
        this.endpoints.forEach { endpoint ->
            endpoint.getOperations().forEach { operation ->
                registerMappingForOperation(endpoint, operation)
            }
        }
    }

    /**
     * 针对于具体的Operation去进行注册RequestMappingInfo
     *
     * @param endpoint endpoint
     * @param webOperation WebOperation
     */
    private fun registerMappingForOperation(endpoint: ExposableWebEndpoint, webOperation: WebOperation) {
        val predicate = webOperation.getRequestPredicate()
        val path = predicate.getPath()

        // 根据RequestPredicate去构建出来一个RequestMappingInfo
        val mappingInfo = createRequestMappingInfo(predicate, path)

        // register RequestMappingInfo到MappingRegistry当中
        // 要使用的HandlerObject为OperationHandler(相当于SpringMVC的Controller)，要使用的HandlerMethod为"handle"方法
        registerMapping(mappingInfo, OperationHandler(MvcWebOperationAdapter(webOperation)), this.handleMethod)
    }

    /**
     * 注册一个Mapping(RequestMappingInfo)到MappingRegistry当中
     *
     * @param requestMappingInfo RequestMappingInfo(Mapping)
     * @param handler Handler
     * @param method method(处理请求的Handler)
     */
    private fun registerMapping(requestMappingInfo: RequestMappingInfo, handler: Any, method: Method) {
        this.mappingRegistry.registerHandlerMethod(handler, method, requestMappingInfo)
    }

    /**
     * 根据Predicate去转换成为RequestMappingInfo
     *
     * @param predicate 请求断言
     * @param path requestPath(需要拼接上前缀才能作为真正的path)
     * @return 构建好的RequestMappingInfo
     */
    private fun createRequestMappingInfo(predicate: WebOperationRequestPredicate, path: String): RequestMappingInfo {
        return RequestMappingInfo.Builder()
            .paths(this.endpointMapping.createSubPath(path))
            .methods(RequestMethod.forName(predicate.getHttpMethod().name))
            .build()
    }

    /**
     * 对于判断isHandler的方式，我们不用去进行匹配，因为我们重写了initHandlerMethods方法，
     * 我们直接自定义了寻找Handler的方式，这个方法不会被回调到，根本不用使用isHandler方法去进行匹配了
     */
    override fun isHandler(beanType: Class<*>) = false

    /**
     * 对于针对具体的方法去创建Mapping的具体逻辑，因为我们直接重写了initHandlerMethods方法，这个方法根本不会被调用到
     */
    override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? = null

    /**
     * Mvc的WebOperation
     */
    @FunctionalInterface
    interface MvcWebOperation {
        fun handle(request: HttpServerRequest, body: Map<String, String>): Any?
    }

    /**
     * 将WebOperation转换为MvcWebOperation的适配器
     *
     * @param operation WebOperation
     */
    class MvcWebOperationAdapter(val operation: WebOperation) : MvcWebOperation {
        override fun handle(request: HttpServerRequest, body: Map<String, String>): Any? {
            val context = InvocationContext(emptyMap())
            return operation.invoke(context)
        }
    }

    /**
     * MvcWebOperation的Handler，提供handle方法，直接去执行"WebMvcOperation.handle"方法，
     * 将它作为一个Controller去注册到MappingRegistry当中，提供的handle方法，需要将返回值设置为@ResponseBody，
     * 需要将Body标注为@RequestBody注解，表示去获取到请求当中的RequestBody，并放入到Map当中
     *
     * @param operation MvcWebOperation
     */
    class OperationHandler(private val operation: MvcWebOperation) {
        @ResponseBody
        fun handle(request: HttpServerRequest, @RequestBody body: Map<String, String>): Any? =
            operation.handle(request, body)
    }
}