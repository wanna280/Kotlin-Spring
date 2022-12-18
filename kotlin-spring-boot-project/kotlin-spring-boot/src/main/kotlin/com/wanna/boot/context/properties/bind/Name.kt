package com.wanna.boot.context.properties.bind

/**
 * 提供对于ValueObject的绑定时, 该参数需要使用到的属性名,
 * 在最终去进行使用时, 将会转换成为dash(破折号)风格.
 * 如果不给定的话, 那么将会使用参数名发现器去进行自动推断
 *
 * @param value property name
 * @see com.wanna.framework.core.DefaultParameterNameDiscoverer
 */
annotation class Name(val value: String)
