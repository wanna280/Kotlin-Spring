package com.wanna.framework.core

import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * 这是一个带有优先级的参数名发现器，它聚合了多个参数名发现器，去配合完成
 */
open class PrioritizedParameterNameDiscoverer : ParameterNameDiscoverer {
    // 参数名发现器列表
    private val parameterNameDiscoverers = ArrayList<ParameterNameDiscoverer>(3)

    /**
     * 往参数名发现器的列表尾部添加参数名的发现器
     */
    open fun addParameterNameDiscoverer(parameterNameDiscoverer: ParameterNameDiscoverer) {
        this.parameterNameDiscoverers += parameterNameDiscoverer
    }

    override fun getParameterNames(constructor: Constructor<*>): Array<String>? {
        parameterNameDiscoverers.forEach {
            val parameterNames = it.getParameterNames(constructor)
            if (parameterNames != null) {
                return parameterNames
            }
        }
        return null
    }

    override fun getParameterNames(method: Method): Array<String>? {
        parameterNameDiscoverers.forEach {
            val parameterNames = it.getParameterNames(method)
            if (parameterNames != null) {
                return parameterNames
            }
        }
        return null
    }
}