package com.wanna.framework.context.event

import com.wanna.framework.core.ResolvableType

/**
 * 这是一个支持存放一个payload的ApplicationEvent，可以支持去发布某些普通的Java对象；
 * 想要发布的对象如果不是ApplicationEvent类型的，就可以使用PayloadApplicationEvent去进行包装一层
 *
 * @param source Event Source
 * @param payload 想要发布的payload事件
 */
open class PayloadApplicationEvent<T : Any>(source: Any?, val payload: T) : ApplicationEvent(source) {

    /**
     * 获取ResolvableType, 提供对于自己的泛型的描述信息;
     * 因为Java的类型擦除, 因为只能在实例化对象之后, 再进行更加具体的判断, 根据Class无法解析到泛型
     *
     * @return ResolveType
     */
    open fun getResolvableType(): ResolvableType {
        return ResolvableType.forClassWithGenerics(this::class.java, payload::class.java)
    }
}