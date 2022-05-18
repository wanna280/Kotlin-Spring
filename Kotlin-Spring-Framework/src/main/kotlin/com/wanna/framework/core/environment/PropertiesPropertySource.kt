package com.wanna.framework.core.environment

import java.util.Properties

/**
 * 这是一个基于Properties的PropertySource，本质上是一个MapPropertySource；Properties本来是支持<Object,Object>的泛型的，但是在PropertySource的实现当中；
 * 已经限制死了key只能是String类型，因此在Properties当中，也被限制了只能通过String类型去作为key来进行value的访问
 *
 * @see MapPropertySource
 * @see PropertySource
 * @see Properties.getProperty
 * @see Properties.setProperty
 */
@Suppress("UNCHECKED_CAST")
open class PropertiesPropertySource(name: String, source: Properties) : MapPropertySource(name, source as Map<String, Any>) {
    // 提供一个使用Map的方式去进行构建的构造器
    constructor(name: String, source: Map<String, Any>) : this(name, source as Properties)
}