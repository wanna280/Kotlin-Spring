package com.wanna.boot.actuate.endpoint.web.mvc

import com.wanna.boot.actuate.endpoint.InvocationContext
import com.wanna.boot.actuate.endpoint.web.EndpointMapping
import com.wanna.boot.actuate.endpoint.web.ExposableWebEndpoint
import com.wanna.boot.actuate.endpoint.web.WebOperation
import com.wanna.boot.actuate.endpoint.web.WebOperationRequestPredicate
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.method.RequestMappingInfo
import com.wanna.framework.web.method.RequestMappingInfoHandlerMapping
import com.wanna.framework.web.bind.annotation.RequestBody
import com.wanna.framework.web.bind.annotation.ResponseBody
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import java.lang.reflect.Method

/**
 * 处理WebMvc的Endpoint的HandlerMapping, 支持将Endpoint当中的Operation方法交给OperationHandler去进行代为调用; 
 *
 * 我们沿用父类的大多数方法, 包括路径的匹配, 以及Mapping也使用RequestMappingInfo, 甚至是
 * 管理Handler方法的MappingRegistry都沿用父类的模板方法, 我们需要重写的, 只是寻找HandlerMethod的方式罢了
 *
 * @see RequestMappingInfoHandlerMapping
 * @see RequestMappingInfo
 * @see OperationHandler
 *
 * @param endpoints  当前HandlerMapping当中已经注册的所有的Endpoint列表
 * @param endpointMapping Endpoint的Mapping
 * @param shouldRegisterLinksMapping 是否应该注册所有的endpoint的链接的映射关系(发现页, Discovery Page)
 */
abstract class AbstractWebMvcEndpointHandlerMapping(
    private val endpoints: List<ExposableWebEndpoint>,
    private val endpointMapping: EndpointMapping,
    private val shouldRegisterLinksMapping: Boolean
) : RequestMappingInfoHandlerMapping() {

    // 处理请求的Handler方法(指向Operation的handle方法)
    private val handlerMethod =
        ReflectionUtils.findMethod(
            OperationHandler::class.java,
            "handle",
            HttpServerRequest::class.java,
            Map::class.java
        )!!

    /**
     * 初始化HandlerMapping, 将所有Endpoint当中的所有的Operation方法全部封装成为RequestMappingInfo,
     * 并将该"RequestMappingInfo-->HandlerMethod"映射关系, 都去自动去注册到MappingRegistry当中
     */
    override fun initHandlerMethods() {
        this.endpoints.forEach { endpoint ->
            endpoint.getOperations().forEach { operation ->
                registerMappingForOperation(endpoint, operation)
            }
        }

        // 如果需要注册链接的映射关系, 那么需要注册一个发现页, 一般默认也就是"/actuator"页面
        // 用来去暴露当前应用当中的所有的endpoint的name以及url信息, 方便用户去进行查询
        if (shouldRegisterLinksMapping) {
            registerLinksMapping()
        }
    }

    /**
     * 注册要去进行暴露的endpoint的链接的映射关系的RequestMapping
     *
     * @see LinksHandler
     */
    private fun registerLinksMapping() {
        val linksHandler = getLinksHandler()
        // 处理endpoint的链接的映射的方法
        val linksMethod = ReflectionUtils.findMethod(
            linksHandler::class.java,
            "links",
            HttpServerRequest::class.java,
            HttpServerResponse::class.java
        )!!
        // build RequestMappingInfo
        val mappingInfo =
            RequestMappingInfo.Builder()
                .paths(endpointMapping.createSubPath(""))
                .methods(RequestMethod.GET)
                .build()

        // 注册一个RequestMapping到MappingRegistry当中, 设置HandlerMethod为"LinksHandler.links"
        registerMapping(mappingInfo, linksHandler, linksMethod)
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
        // 要使用的HandlerObject为OperationHandler(相当于SpringMVC的Controller), 要使用的HandlerMethod为"handle"方法
        registerMapping(mappingInfo, OperationHandler(MvcWebOperationAdapter(webOperation)), this.handlerMethod)
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
     * 对于判断isHandler的方式, 我们不用去进行匹配, 因为我们重写了initHandlerMethods方法,
     * 我们直接自定义了寻找Handler的方式, 这个方法不会被回调到, 根本不用使用isHandler方法去进行匹配了
     */
    override fun isHandler(beanType: Class<*>) = false

    /**
     * 对于针对具体的方法去创建Mapping的具体逻辑, 因为我们直接重写了initHandlerMethods方法, 这个方法根本不会被调用到
     */
    override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? = null

    /**
     * 获取LinksHandler, 交给子类去完成
     *
     * @return 你想要使用的LinksHandler
     */
    protected abstract fun getLinksHandler(): LinksHandler

    /**
     * Mvc的WebOperation
     */
    @FunctionalInterface
    interface MvcWebOperation {
        fun handle(request: HttpServerRequest, body: Map<String, String>?): Any?
    }

    /**
     * 将WebOperation转换为MvcWebOperation的适配器
     *
     * @param operation WebOperation
     */
    class MvcWebOperationAdapter(private val operation: WebOperation) : MvcWebOperation {
        override fun handle(
            request: HttpServerRequest,
            @RequestBody(required = false) body: Map<String, String>?
        ): Any? {
            val arguments = getArguments(request, body)
            val context = InvocationContext(arguments)
            return operation.invoke(context)
        }

        /**
         * 获取request当中的各种类型的参数列表(PathVariables/RequestParam/RequestBody)
         *
         * @param request request
         * @param body RequestBody
         * @return 从请求当中解析出来的参数列表
         */
        @Suppress("UNCHECKED_CAST")
        private fun getArguments(request: HttpServerRequest, body: Map<String, String>?): Map<String, Any> {
            val arguments = HashMap<String, Any>()
            val urlVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)
            // 1.add Url Template Variables(PathVariables)
            if (urlVariables is Map<*, *>) {
                arguments.putAll(urlVariables as Map<String, Any>)
            }
            // 2.add Request Body
            if (body != null && request.getMethod() == RequestMethod.POST) {
                arguments.putAll(body)
            }
            // 3.add Request Param
            request.getParamMap().forEach { (name, value) -> arguments[name] = if (value.size > 1) value else value[0] }
            return arguments
        }
    }

    /**
     * MvcWebOperation的Handler, 提供handle方法, 直接去执行"WebMvcOperation.handle"方法,
     * 将它作为一个Controller去注册到MappingRegistry当中, 提供的handle方法, 需要将返回值设置为@ResponseBody,
     * 需要将Body标注为@RequestBody注解, 表示去获取到请求当中的RequestBody, 并放入到Map当中
     *
     * @param operation MvcWebOperation
     */
    class OperationHandler(private val operation: MvcWebOperation) {
        @ResponseBody
        fun handle(request: HttpServerRequest, @RequestBody(required = false) body: Map<String, String>?): Any? =
            operation.handle(request, body)
    }

    /**
     * LinksHandler
     */
    interface LinksHandler {
        fun links(request: HttpServerRequest, response: HttpServerResponse): Any
    }
}