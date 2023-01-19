package com.wanna.framework.web.method.support

import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.ParameterNameDiscoverer
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.method.HandlerMethod
import com.wanna.common.logging.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.Callable

/**
 * 这是一个可以被执行的HandlerMethod, 提供了invokeAndHandle方法, 外部可以直接调用, 去完成方法的调用
 *
 * @see invokeAndHandle
 * @see HandlerMethod
 */
open class InvocableHandlerMethod() : HandlerMethod() {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(InvocableHandlerMethod::class.java)

        /**
         * 使用反射去寻找到Callable的call方法
         */
        @JvmStatic
        private val CALLABLE_METHOD = ReflectionUtils.findMethod(Callable::class.java, "call")!!

        /**
         * 空参数的常量
         */
        @JvmStatic
        private val EMPTY_ARGS = emptyArray<Any?>()
    }

    /**
     * 参数名发现器, 提供参数名的解析功能
     */
    var parameterNameDiscoverer: ParameterNameDiscoverer = DefaultParameterNameDiscoverer()

    /**
     * 参数解析器列表
     */
    @Nullable
    var argumentResolvers: HandlerMethodArgumentResolverComposite? = HandlerMethodArgumentResolverComposite()

    /**
     * 返回值解析器列表
     */
    @Nullable
    var returnValueHandlers: HandlerMethodReturnValueHandlerComposite? = HandlerMethodReturnValueHandlerComposite()

    /**
     * BinderFactory(提供了参数类型的转换等功能)
     */
    @Nullable
    var binderFactory: WebDataBinderFactory? = null

    /**
     * 基于一个已经有的[HandlerMethod]去进行构建[InvocableHandlerMethod]
     *
     * @param handlerMethod 已经有的HandlerMethod
     */
    constructor(handlerMethod: HandlerMethod) : this() {
        this.method = handlerMethod.method
        this.parameters = handlerMethod.parameters
        this.beanType = handlerMethod.beanType
        this.beanFactory = handlerMethod.beanFactory
        this.handlerMethod = handlerMethod
        this.bean = handlerMethod.bean
    }

    /**
     * 提供一个基于bean和method去构建一个[InvocableHandlerMethod]的构造方法
     *
     * @param bean bean
     * @param method method
     */
    constructor(bean: Any, method: Method) : this() {
        this.bean = bean
        this.method = method

        // 初始化parameters和beanType
        this.parameters = Array(method.parameterCount) { MethodParameter(method, it) }
        this.beanType = ClassUtils.getUserClass(bean)
    }

    /**
     * 执行目标HandlerMethod, 并处理目标方法的返回值
     *
     * @param webRequest NativeWebRequest(request and response)
     * @param mavContainer ModelAndView容器
     * @param provideArgs 外部提供的参数列表, 在进行参数解析时, 优先从给定的参数列表当中获取
     */
    open fun invokeAndHandle(
        webRequest: ServerWebRequest, mavContainer: ModelAndViewContainer, vararg provideArgs: Any
    ) {
        // 遍历所有的ArgumentResolver去解析方法参数, 并反射执行目标方法
        val returnValue = invokeForRequest(webRequest, mavContainer, *provideArgs)

        if (returnValue == null) {
            if (mavContainer.responseStatus != null) {
                mavContainer.requestHandled = true
                return
            }
        }

        mavContainer.requestHandled = false
        val returnValueType = getReturnValueType(returnValue)

        // 遍历所有的ReturnValueHandler, 去找到合适的一个去进行方法的返回值的处理...
        try {
            this.returnValueHandlers?.handleReturnValue(returnValue, webRequest, returnValueType, mavContainer)
        } catch (ex: Exception) {
            if (logger.isTraceEnabled) {
                logger.trace("处理方法的返回值失败, 原因是[${ex.message}]")
            }
            throw ex
        }
    }


    /**
     * 使用HandlerMethod当中组合的参数解析器去获取HandlerMethod执行时, 应该用到的参数值列表
     *
     * @param webRequest NativeWebRequest(request and response)
     * @param mavContainer ModelAndViewContainer
     * @param provideArgs 外部提供的参数列表, 在进行参数解析时, 优先从给定的参数列表当中获取
     * @return 当前HandlerMethod的参数列表params
     * @throws IllegalStateException 如果没有找到合适的方法的参数解析器去解析当前的方法参数
     * @throws Exception 如果使用参数解析器解析过程当中发生了异常
     */
    protected open fun getMethodArgumentValues(
        webRequest: NativeWebRequest, mavContainer: ModelAndViewContainer?, vararg provideArgs: Any
    ): Array<Any?> {
        val methodParameters = parameters
        if (methodParameters == null || methodParameters.isEmpty()) {
            return EMPTY_ARGS
        }
        val params = arrayOfNulls<Any?>(methodParameters.size)
        val resolvers = argumentResolvers
        (methodParameters.indices).forEach {
            val parameter = methodParameters[it]
            // 先尝试从外部提供的参数列表当中去进行寻找类型匹配的参数(比如@ExceptionHandler需要用到具体的异常信息, 就支持从这里去进行给定)
            params[it] = findProvidedArgument(parameter, *provideArgs)
            // 如果找到了, 那么就不使用参数解析器去进行匹配了; 如果没有找到, 那么就得交给参数解析器去完成解析了
            if (params[it] != null) {
                return@forEach
            }
            // 在解析之前, 需要先初始化参数名发现器, 方便在解析参数的值时, 可以去获取参数的name
            parameter.initParameterNameDiscoverer(this.parameterNameDiscoverer)
            // 交给方法参数解析器列表, 挨个尝试去解析方法参数
            if (resolvers != null) {
                if (!resolvers.supportsParameter(parameter)) {
                    throw IllegalStateException("解析方法参数[$parameter]失败, 原因是没有找到合适的参数解析器去进行解析")
                }
                try {
                    params[it] = resolvers.resolveArgument(parameter, webRequest, mavContainer, binderFactory)
                } catch (ex: Exception) {
                    if (logger.isDebugEnabled) {
                        logger.debug("使用参数解析器去解析参数[$parameter]失败, 原因是[${ex.message}]", ex)
                    }
                    throw ex
                }
            }
        }
        return params
    }

    /**
     * 从提供的参数当中, 找到类型匹配的参数
     *
     * @param parameter 目标参数信息
     * @param provideArgs 外部提供的参数列表, 在进行参数解析时, 优先从给定的参数列表当中获取
     * @return 如果从候选的参数列表当中找到了合适的参数, 那么return该参数; 如果没有匹配的, return null
     */
    @Nullable
    protected open fun findProvidedArgument(parameter: MethodParameter, vararg provideArgs: Any): Any? {
        provideArgs.forEach {
            if (parameter.getParameterType().isInstance(it)) {
                return it
            }
        }
        return null
    }

    /**
     * 解析HandlerMethod方法参数, 并执行目标方法
     *
     * @param webRequest NativeWebRequest(request and response)
     * @param mavContainer ModelAndView容器
     * @param provideArgs 外部提供的参数列表, 在进行参数解析时, 优先从给定的参数列表当中获取
     * @return 执行目标Handler方法的返回值
     */
    @Nullable
    open fun invokeForRequest(
        webRequest: NativeWebRequest, @Nullable mavContainer: ModelAndViewContainer?, vararg provideArgs: Any
    ): Any? {
        val args = getMethodArgumentValues(webRequest, mavContainer, *provideArgs)
        if (logger.isTraceEnabled) {
            logger.trace("执行HandlerMethod的方法参数列表为[${args.contentToString()}]")
        }
        return doInvoke(*args)
    }

    /**
     * 使用反射的方式去执行目标方法
     *
     * @param args 目标handlerMethod的参数列表
     * @return handlerMethod的执行结果的返回值
     */
    @Nullable
    protected open fun doInvoke(vararg args: Any?): Any? {
        val method = method ?: throw IllegalStateException("HandlerMethod当中方法为null")
        ReflectionUtils.makeAccessible(method)
        try {
            return ReflectionUtils.invokeMethod(method, this.bean, *args)
        } catch (ex: IllegalArgumentException) {
            throw IllegalArgumentException(
                "执行HandlerMethod出现了不合法参数, [method=${method}, args=${args.contentToString()}]",
                ex
            )
        } catch (ex: InvocationTargetException) {
            throw IllegalStateException("执行目标方法发生错误, 原因是-->${ex.targetException}", ex)
        }
    }

    /**
     * 包装异步的执行结果成为一个HandlerMethod, 并将返回值封装成为一个MethodParameter,
     * 设置匹配的注解是从原来的HandlerMethod上去进行搜索, 而不是从Callable的call方法上去进行搜索
     *
     * @param result 异步任务处理的最终结果
     * @return InvocableHandlerMethod
     */
    open fun wrapConcurrentResult(@Nullable result: Any?): InvocableHandlerMethod {
        val concurrentResultHandlerMethod = ConcurrentResultHandlerMethod(result, ReturnValueMethodParameter(result))
        // copy returnValueHandlers
        if (this.returnValueHandlers != null) {
            concurrentResultHandlerMethod.returnValueHandlers = returnValueHandlers
        }
        return concurrentResultHandlerMethod
    }


    /**
     * 处理并发任务的HandlerMethod, 主要用于去替换掉原本的HandlerMethod, 转换成为我们自定义的Handler的方式去处理本次请求
     *
     * @param result 异步任务执行的结果
     * @param returnType 该方法的返回类型封装成为的返回值类型
     */
    private inner class ConcurrentResultHandlerMethod(result: Any?, private val returnType: MethodParameter) :
        InvocableHandlerMethod() {
        init {
            this.bean = Callable {
                if (result is Throwable) {
                    throw result
                }
                result
            }
            this.method = CALLABLE_METHOD
        }

        /**
         * 重写获取方法上的注解的方法, 沿用外部类的寻找注解的方式去进行寻找
         *
         * @param annotationType annotationType
         */
        override fun <T : Annotation> getMethodAnnotation(annotationType: Class<T>) =
            this@InvocableHandlerMethod.getMethodAnnotation(annotationType)

        /**
         * 判断该方法上是否有存在该注解? 
         *
         * @param annotationClass 要去进行匹配的注解
         * @return 如果该方法上有该注解, 那么return true; 否则return false
         */
        override fun hasMethodAnnotation(annotationClass: Class<out Annotation>) =
            this@InvocableHandlerMethod.hasMethodAnnotation(annotationClass)

        override fun getReturnValueType(@Nullable returnValue: Any?) = this.returnType
    }
}