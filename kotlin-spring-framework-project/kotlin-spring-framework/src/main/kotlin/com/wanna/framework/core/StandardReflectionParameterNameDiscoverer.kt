package com.wanna.framework.core

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Parameter

/**
 * 这是标准反射的参数名的发现器, 基于JDK1.8当中的"-parameters"参数设置为true时去进行的实现;
 * 可以通过Parameter的isNamePresent标志位去进行判断是否拥有真实的参数名？
 */
open class StandardReflectionParameterNameDiscoverer : ParameterNameDiscoverer {
    override fun getParameterNames(constructor: Constructor<*>): Array<String>? {
        return doGetParameterNames(constructor.parameters)
    }

    override fun getParameterNames(method: Method): Array<String>? {
        return doGetParameterNames(method.parameters)
    }

    /**
     * 根据给定的Parameter列表, 去匹配Parameter的参数名
     *
     * @param parameters 要匹配的Parameter列表
     * @return 如果有真实的参数名的话, return参数名列表; 不然return null
     */
    @Suppress("UNCHECKED_CAST")
    private fun doGetParameterNames(parameters: Array<Parameter>): Array<String>? {
        val parameterNames = arrayOfNulls<String>(parameters.size)
        for (index in parameters.indices) {
            val parameter = parameters[index]
            // 如果根本不存在存在有真实的name(获取到的参数名为arg0, arg1...), 那么return null
            if (!parameter.isNamePresent) {
                return null
            }
            parameterNames[index] = parameter.name
        }
        return parameterNames as Array<String> // cast to not null array
    }
}