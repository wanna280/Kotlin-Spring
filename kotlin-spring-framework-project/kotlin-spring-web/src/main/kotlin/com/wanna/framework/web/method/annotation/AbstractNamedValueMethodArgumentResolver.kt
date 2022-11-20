package com.wanna.framework.web.method.annotation

import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.bind.annotation.ValueConstants
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.ModelAndViewContainer
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

    /**
     * beanFactory
     */
    private var beanFactory: ConfigurableBeanFactory? = null

    /**
     * NamedValueInfo缓存，key-方法参数，value-要处理的NamedValueInfo
     */
    private val namedValueInfoCache = ConcurrentHashMap<MethodParameter, NamedValueInfo>()

    /**
     * 解析一个方法参数的值
     *
     * @param parameter 需要去进行解析的方法参数
     * @param webRequest NativeWebRequest
     * @param mavContainer ModelAndViewContainer
     * @param binderFactory BinderFactory
     */
    @Nullable
    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        @Nullable mavContainer: ModelAndViewContainer?,
        @Nullable binderFactory: WebDataBinderFactory?
    ): Any? {
        // 获取NamedValueInfo，交给子类去进行注解的解析并构建NamedValueInfo
        val namedValueInfo = getNamedValueInfo(parameter)

        // 如果必要的话，使用beanFactory去将name使用嵌入式的值解析器的方式去进行解析
        val resolvedName = resolveEmbeddedValuesAndExpressions(namedValueInfo.name)
            ?: throw IllegalArgumentException("无法解析到方法的参数名[${namedValueInfo.name}]")

        // 交给子类去解析该参数名的值
        var arg = resolveName(resolvedName.toString(), webRequest)
        if (arg == null) {
            // 如果有默认值得话, 去进行value的占位符解析工作(在之前已经将defaultValue从"DEFAULT_NONE"转换成为null)
            if (namedValueInfo.defaultValue != null) {
                arg = resolveEmbeddedValuesAndExpressions(namedValueInfo.defaultValue)
            } else {
                handleMissingValue(resolvedName.toString(), parameter, webRequest)
            }
        }
        if (binderFactory != null) {
            val binder = binderFactory.createBinder(webRequest, null, resolvedName.toString())

            return binder.convertIfNecessary(arg, parameter.getParameterType())
        }
        return arg
    }

    /**
     * 处理值缺失的情况
     *
     * @param name name
     * @param parameter parameter
     * @param webRequest NativeWebRequest
     */
    protected open fun handleMissingValue(name: String, parameter: MethodParameter, webRequest: NativeWebRequest) {
        handleMissingValue(name, parameter)
    }

    /**
     * 处理值缺失的情况
     *
     * @param name name
     * @param parameter parameter
     */
    protected open fun handleMissingValue(name: String, parameter: MethodParameter) {

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
     *
     * @param expression 待进行解析的表达式
     * @return 解析表达式得到的结果
     */
    @Nullable
    private fun resolveEmbeddedValuesAndExpressions(expression: String): Any? {
        return this.beanFactory?.resolveEmbeddedValue(expression) ?: expression
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
     * Note: MethodParameter，需要提前初始化参数名解析器，才能去去获取到参数名
     * @param parameter 方法参数
     * @param namedValueInfo 子类构建的NamedValueInfo
     * @return 重新构建的NamedValueInfo(将defaultValue从"DEFAULT_NONE"转换成为null)
     */
    private fun updateNamedValueInfo(parameter: MethodParameter, namedValueInfo: NamedValueInfo): NamedValueInfo {
        var name: String? = namedValueInfo.name
        if (name == null || name.isEmpty()) {
            // 如果没有指定name的话，需要获取参数名name
            name = parameter.getParameterName()
            if (name == null) {
                throw IllegalArgumentException("解析参数名失败")
            }
        }

        // 将defaultValue从"DEFAULT_NONE"转换成为null
        val defaultValue =
            if (namedValueInfo.defaultValue == ValueConstants.DEFAULT_NONE) null else namedValueInfo.defaultValue
        return NamedValueInfo(name, namedValueInfo.required, defaultValue)
    }

    /**
     * 如何去创建NamedValueInfo? 交给子类去实现，用来去获取注解当中的相关信息
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