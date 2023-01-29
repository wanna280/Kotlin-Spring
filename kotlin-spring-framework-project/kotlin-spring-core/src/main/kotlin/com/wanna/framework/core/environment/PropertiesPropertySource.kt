package com.wanna.framework.core.environment

import java.util.*

/**
 * 这是一个基于JDK的[Properties]进行实现的[PropertySource], 本质上是一个[MapPropertySource];
 * [Properties]本来是支持`<Object,Object>`这样的泛型的, 但是在[PropertySource]的实现当中;
 * 已经限制死了key只能是String类型, 因此在[Properties]当中, 也被限制了只能通过String类型去作为key来进行value的访问
 *
 * @see MapPropertySource
 * @see PropertySource
 * @see Properties.getProperty
 * @see Properties.setProperty
 *
 * @param name PropertySource name
 * @param source source(Map)
 */
@Suppress("UNCHECKED_CAST")
open class PropertiesPropertySource(name: String, source: Map<String, Any>) : MapPropertySource(name, source) {
    /**
     * 提供一个使用Map的方式去进行构建的构造器
     *
     * @param name PropertySource name
     * @param source Properties
     */
    constructor(name: String, source: Properties) : this(name, source as Map<String, Any>)
}