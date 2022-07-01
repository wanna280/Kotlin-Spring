package com.wanna.framework.context.event

/**
 * 这是一个支持存放一个payload的ApplicationEvent，可以支持去发布某些普通的Java对象；
 * 想要发布的对象如果不是ApplicationEvent类型的，就可以使用PayloadApplicationEvent去进行包装一层
 *
 * @param source Event Source
 * @param payload 想要发布的payload事件
 */
open class PayloadApplicationEvent<T>(source: Any?, val payload: T) : ApplicationEvent(source)