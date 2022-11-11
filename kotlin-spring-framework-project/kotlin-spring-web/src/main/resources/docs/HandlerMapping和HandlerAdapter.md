# 1. HandlerMapping与HandlerAdapter

## 1.1 HandlerMapping

HandlerMapping，顾名思义"Handler的Mapping"，也就是SpringMVC的请求处理器的映射，它维护了

* 1.所有的路径规则对应的请求的处理器(key-path,value-Handler的一个Map)
* 2.SpringMVC的拦截器(HandlerInterceptor)列表

HandlerMapping的接口抽象定义如下：

```kotlin
interface HandlerMapping {
    fun getHandler(request: HttpServerRequest): HandlerExecutionChain?
}
```

HandlerMapping主要作用是，给定request，根据url、header等策略决策出来合适的Handler去处理请求。 这里的Handler是Any(Object)类型的，因为并没有办法决定Handler的上层应该是什么类型。

但是我们在HandlerMapping的接口定义当中，返回值是HandlerExecutionChain，是为什么呢？原因在于`HandlerExecutionChain=Handler+HandlerInterceptor`。对于每个HandlerMapping来说，都集成了SpringMVC的拦截器列表，它在返回Handler时，应该将拦截器也一并去进行返回。

```kotlin
open class HandlerExecutionChain(private val handler: Any, interceptors: Collection<HandlerInterceptor>? = null) {

    private val interceptorList = ArrayList<HandlerInterceptor>()

    private var interceptorIndex = -1

    init {
        if (interceptors != null) {
            interceptorList += interceptors
        }
    }
    open fun applyPreHandle(request: HttpServerRequest, response: HttpServerResponse): Boolean {
        interceptorList.indices.forEach {
            val interceptor = interceptorList[it]
            if (!interceptor.preHandle(request, response, this.handler)) {
                triggerAfterCompletion(request, response, null)
                return false
            }
            interceptorIndex = it  // update interceptorIndex
        }
        return true
    }
    
    open fun applyPostHandle(request: HttpServerRequest, response: HttpServerResponse) {
        interceptorList.indices.reversed().forEach {
            val interceptor = interceptorList[it]
            interceptor.postHandle(request, response, this.handler)
        }
    }
    
    open fun triggerAfterCompletion(request: HttpServerRequest, response: HttpServerResponse, ex: Throwable?) {
        (0..interceptorIndex).reversed().forEach {
            val interceptor = interceptorList[it]
            try {
                interceptor.afterCompletion(request, response, this.handler, ex)
            } catch (ex2: Throwable) {
                // log...
            }
        }
    }

    open fun getHandler(): Any = this.handler
}
```

一般来说，每个HandlerMapping会返回一个自家的类型的Handler，比如：

* 1.RequestMappingHandlerMapping，它负责维护的就是`@RequestMapping`的路径-`@RequestMapping`的方法(称为HandlerMethod)的映射关系。
* 2.SimpleUrlHandlerMapping它支持匹配SpringMVC的静态资源(比如我们常说的"resources/public/"、"resources/static/"等，就是交给它去处理的)，并返回一个`HttpRequestHandler`作为Handler。

在SpringMVC当中，因为需要集成多种策略去进行HandlerMapping的匹配，因此就需要使用到策略模式，挨个去匹配所有的HandlerMapping，直到找到一个合适的处理本次请求的Handler。代码如下：

```kotlin
    protected open fun getHandler(request: HttpServerRequest): HandlerExecutionChain? {
        this.handlerMappings?.forEach {
            val handler = it.getHandler(request)
            if (handler != null) {
                return handler
            }
        }
        return null
    }
```

需要遍历所有的HandlerMapping，去找到一个能处理当前的request的Handler。如果当前HandlerMapping不能支持处理，那么换下一个HandlerMapping，直到最终匹配到一个合适的Handler。如果遍历完所有的HandlerMapping，都没有找到合适的Handler，在`Dispatcher`当中，就会发送404的错误给客户端。

## 1.2 HandlerAdapter

在HandlerMapping当中，我们已经介绍到，不同的HandlerMapping会返回不同类型的Handler(比如HandlerMethod/HttpRequestHandler)。那么对于返回回来的Handler，我`Dispatcher`应该怎么处理？我怎么知道怎么处理？(因为没有一层合适的接口抽象，我连要调用你Handler的哪个方法都不知道)

因此，一般只要HandlerMapping返回了一种类型的Handler，那么我就需要写一个对应的处理该Handler的策略。对于处理该Handler的策略，就是HandlerAdapter。

* 1.对于RequestMappingHandlerMapping，它返回的类型是HandlerMethod，因此我就必须写一个策略去处理HandlerMethod。而这个策略，就是RequestMappingHandlerAdapter。
* 2.由于HandlerMapping，还会有别的返回类型，比如HttpRequestHandler，我们也应该写一个策略去处理该类型的Handler，也就是HttpRequestHandlerAdapter。

由于，HandlerMapping返回的类型并不能确定，因此我们也并不知道应该使用哪个HandlerAdapter去处理该类型的Handler，因此对于HandlerAdapter的设计，我们也应该采用策略模式的设计。代码如下：

```kotlin
    protected open fun getHandlerAdapter(handler: Any): HandlerAdapter {
        this.handlerAdapters?.forEach {
            if (it.supports(handler)) {
                return it
            }
        }
        throw IllegalStateException("No Suitable HandlerAdapter...")
    }
```

我们要做的，也是遍历所有的HandlerAdapter，看哪个HandlerAdapter可以处理当前类型的Handler。

那么你是否有这样的疑问，既然HandlerMapping需要返回一个Handler，该Handler又得交给HandlerAdapter去进行处理，是不是很多余？既然是一对一的，我们为啥不直接放在一个类里去做？

其实，在基于这样的HandlerMapping和HandlerAdapter的设计下，其实我们可以发现，我们当然也可以自定义一个HandlerMapping，让它返回HandlerMethod，直接交给Dispatcher去进行处理。而Dispatcher当中，又恰好有这样的HandlerAdapter去处理HandlerMethod，因此Dispatcher也能处理我们的请求。 也就是说，HandlerMapping与HandlerAdapter之间的关系并不是一对一的，只要保证HandlerMapping产生的Handler，有合适的HandlerAdapter去进行处理即可，在后续的扩展当中，完全可以基于已经有的基础设施去进行扩展，让整体的耦合度变得非常低。对应的就是，也许HandlerMapping的种类，可以远多于HandlerAdapter的种类，可以多个HandlerMapping产生同一个类型的Handler。


