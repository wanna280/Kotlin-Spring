package com.wanna.framework.web.handler

import com.wanna.framework.context.aware.BeanNameAware
import com.wanna.framework.core.Ordered
import com.wanna.framework.web.HandlerExecutionChain
import com.wanna.framework.web.HandlerInterceptor
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.context.WebApplicationObjectSupport
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 这是一个抽象的HandlerMapping，为各个HandlerMapping的实现提供了模板，它实现了ApplicationContextAware，能自动注入ApplicationContext对象；
 * 它负责将handler和HandlerInterceptor列表去封装成为HandlerExecutionChain，它提供了对HandlerInterceptor列表的扩展方法，可以交给子类去进行扩展；
 * 它也支持去自动从ApplicationContext当中去侦测MappedHandlerInterceptor类型的Bean，加入到HandlerInterceptor列表当中；
 *
 * 对于handler的具体的落地获取方式，它为子类提供了getHandlerInternal方法，子类可以通过getHandlerInternal这个抽象模板方法去实现要子类当中要自定义的
 * 获取处理本次HTTP请求的Handler对象的方式，比如可以在这里返回一个HandlerMethod对象作为处理请求的最终Handler
 *
 * @see getHandler
 * @see getHandlerInternal
 */
abstract class AbstractHandlerMapping : HandlerMapping, Ordered, BeanNameAware, WebApplicationObjectSupport() {

    private var order: Int = Ordered.ORDER_LOWEST

    private var beanName: String? = null

    // 拦截器列表
    private val interceptors: ArrayList<Any> = ArrayList()

    // 经过了类型转换之后的拦截器列表
    private val adaptedInterceptors: ArrayList<HandlerInterceptor> = ArrayList()

    override fun getHandler(request: HttpServerRequest): HandlerExecutionChain? {
        val handler = getHandlerInternal(request)
        if (handler == null) {
            return null
        }

        val handlerExecutionChain = getHandlerExecutionChain(request, handler)

        return handlerExecutionChain
    }

    /**
     * 获取真正的内部的Handler，交给子类去进行实现
     *
     * @param request request
     * @return handler
     */
    protected abstract fun getHandlerInternal(request: HttpServerRequest): Any?

    /**
     * 将Handler和所有的HandlerInterceptor包装到HandlerExecutionChain当中并进行return
     *
     * @param request request
     * @param handler handler
     * @return HandlerExecutionChain
     */
    protected open fun getHandlerExecutionChain(request: HttpServerRequest, handler: Any): HandlerExecutionChain {
        val handlerExecutionChain = if (handler is HandlerExecutionChain) handler else HandlerExecutionChain(handler)
        this.adaptedInterceptors.forEach {
            handlerExecutionChain.addInterceptor(it)
        }
        return handlerExecutionChain
    }

    override fun setBeanName(beanName: String) {
        this.beanName = beanName
    }

    override fun getOrder(): Int {
        return this.order
    }

    fun setOrder(order: Int) {
        this.order = order
    }

    override fun initApplicationContext() {
        extendsInterceptors(this.interceptors)
        detectMappedInterceptors(this.adaptedInterceptors)
        initInterceptors()
    }

    /**
     * 交给子类去进行重写，去扩展Interceptors，往给定的这个列表当中添加元素即可添加
     */
    protected open fun extendsInterceptors(interceptors: MutableList<Any>) {

    }

    /**
     * 从ApplicationContext当中去探测所有的MappedInterceptors，加入到HandlerInterceptor列表当中
     */
    protected open fun detectMappedInterceptors(mappedInterceptors: MutableList<HandlerInterceptor>) {
        mappedInterceptors.addAll(obtainApplicationContext().getBeansForType(MappedInterceptor::class.java).values)
    }

    /**
     * 初始化Interceptors列表
     */
    protected open fun initInterceptors() {

    }
}