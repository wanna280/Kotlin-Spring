package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.framework.lang.Nullable

/**
 * Java对象的属性绑定的Handler, 相当于是一个监听器, 监听Java对象绑定的各个阶段, 去进行自定义处理
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
interface BindHandler {

    companion object {

        /**
         * 默认的BindHandler单例对象
         */
        @JvmField
        val DEFAULT = object : BindHandler {

        }
    }

    /**
     * 当属性开始去进行绑定时
     */
    @Nullable
    fun <T : Any> onStart(name: ConfigurationPropertyName, target: Bindable<T>, context: BindContext): Bindable<T>? {
        return target
    }

    /**
     * 当属性绑定成功时
     */
    @Nullable
    fun onSuccess(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        context: BindContext,
        @Nullable result: Any?
    ): Any? {
        return result
    }

    /**
     * 当开始创建Java对象时
     */
    @Nullable
    fun onCreate(name: ConfigurationPropertyName, target: Bindable<Any>, @Nullable result: Any?): Any? {
        return result
    }

    /**
     * 当绑定失败时
     */
    fun onFailure(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        context: BindContext,
        error: Exception
    ): Any {
        return target
    }

    /**
     * 当绑定结束时
     */
    fun onFinish(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        context: BindContext,
        @Nullable result: Any?
    ) {

    }

}