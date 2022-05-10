package com.wanna.framework.web.method.annotation

import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个抽象的支持Name-Value的方法参数解析器，它主要针对于有存在有Name-Value的参数去进行解析；
 * 比如RequestParam、RequestHeader等就是典型的Name-Value模式的参数，它们都可以使用此类作为基类去进行处理；
 *
 * 此类的所有子类当中需要提供实现的方法：
 * (1)在createNamedValueInfo方法当中，告诉本类NamedValueInfo如何去进行解析
 * (2)在resolveName方法当中，如何根据根据name去解析出来value的值
 * (3)在supportsParameter方法当中，判断当前参数你是否支持解析？
 *
 * @see RequestParamMethodArgumentResolver
 * @see RequestHeaderMethodArgumentResolver
 */
abstract class AbstractNamedValueMethodArgumentResolver : HandlerMethodArgumentResolver {

    // beanFactory
    private var beanFactory: ConfigurableBeanFactory? = null

    // NamedValueInfo缓存
    private val namedValueInfoCache = ConcurrentHashMap<MethodParameter, NamedValueInfo>()

    /**
     * 解析一个方法参数的值
     */
    override fun resolveArgument(parameter: MethodParameter, webRequest: NativeWebRequest): Any? {
        // 获取NamedValueInfo，交给子类去进行注解的解析并构建NamedValueInfo
        val namedValueInfo = getNamedValueInfo(parameter)

        // 如果必要的话，使用beanFactory去将name使用嵌入式的值解析器的方式去进行解析
        val resolvedName = resolveEmbeddedValuesAndExpressions(namedValueInfo.name)
        if (resolvedName == null) {
            throw IllegalArgumentException("无法解析到方法的参数名[${namedValueInfo.name}]")
        }

        // 交给子类去解析该参数名的值
        val arg = resolveName(resolvedName.toString(), webRequest)
        if (arg == null) {

        }
        return arg
    }

    /**
     * 给定了参数名(name)，需要去解析该参数名对应的值(value)，解析该参数的具体逻辑交给子类去进行实现
     *
     * @param name paramName
     * @param webRequest NativeWebRequest(request and response)
     */
    protected abstract fun resolveName(name: String, webRequest: NativeWebRequest): Any?

    /**
     * 解析嵌入式的值以及表达式
     */
    private fun resolveEmbeddedValuesAndExpressions(name: String): Any? {
        val beanFactory = this.beanFactory
        if (beanFactory != null) {
            return beanFactory.resolveEmbeddedValue(name)
        }
        return name
    }

    /**
     * 获取NamedValueInfo，如果没有的话，交给子类去进行构建
     *
     * @param parameter 方法参数
     * @return NamedValueInfo
     */
    protected open fun getNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        var namedValueInfo = namedValueInfoCache[parameter]
        if (namedValueInfo == null) {
            namedValueInfo = createNamedValueInfo(parameter)
            namedValueInfo = updateNamedValueInfo(parameter, namedValueInfo)
            namedValueInfoCache[parameter] = namedValueInfo
        }
        return namedValueInfo
    }

    /**
     * 更新NamedValue，如果没有解析到NamedValueInfo的name的话，需要从方法参数当中去获取name
     *
     * <note>MethodParameter，需要提前初始化参数名解析器，才能去去获取到参数名</note>
     * @param parameter 方法参数
     * @param namedValueInfo 子类构建的NamedValueInfo
     * @return 重新构建的NamedValueInfo
     */
    private fun updateNamedValueInfo(parameter: MethodParameter, namedValueInfo: NamedValueInfo): NamedValueInfo {
        var name: String? = namedValueInfo.name
        if (name == null || name.isEmpty()) {
            name = parameter.getParameterName()
            if (name == null) {
                throw IllegalArgumentException("解析参数名失败")
            }
        }
        return NamedValueInfo(name, true, null)
    }

    /**
     * 如何去创建NamedValueInfo？交给子类去实现，用来去获取注解当中的相关信息
     *
     * @param parameter 方法参数
     * @return 构建好的NamedValueInfo
     */
    protected abstract fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo

    /**
     * 这是对Name-Value模式的参数的封装，包括一个参数当中的name、required以及defaultValue；这些信息主要从相关的注解头上去进行获取
     *
     * @see RequestParam
     * @see RequestHeader
     *
     * @param name paramName
     * @param required 是否是必须的？
     * @param defaultValue 默认值
     */
    protected open class NamedValueInfo(val name: String, val required: Boolean, val defaultValue: String?)
}