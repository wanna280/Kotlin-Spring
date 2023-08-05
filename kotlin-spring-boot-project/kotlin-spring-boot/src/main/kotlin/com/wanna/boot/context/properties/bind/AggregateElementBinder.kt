package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.boot.context.properties.source.ConfigurationPropertySource
import com.wanna.framework.lang.Nullable

/**
 * 用于[Binder]去进行聚合的元素的递归绑定, 因为在[ArrayBinder]内部需要调用[Binder]的相关方法去进行绑定,
 * 因此需要提供一个Callback回调方法, 而对于[AggregateElementBinder]就是提供这么一个Callback回调方法;
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @see Binder
 */
fun interface AggregateElementBinder {

    /**
     * 使用给定的属性Key的前缀配置信息, 对应的属性值, 去绑定到给定的[Bindable]当中去
     *
     * @param name 属性Key前缀
     * @param target 要去进行绑定的目标信息Bindable
     * @return 绑定完成的实例对象(或者null)
     */
    @Nullable
    fun bind(name: ConfigurationPropertyName, target: Bindable<Any>): Any? = this.bind(name, target, null)

    /**
     * 使用给定的属性Key的前缀配置信息, 对应的属性值, 去绑定到给定的[Bindable]当中去
     *
     * @param name 属性Key前缀
     * @param target 要去进行绑定的目标信息Bindable
     * @param source ConfigurationPropertySource
     * @return 绑定完成的实例对象(或者null)
     */
    @Nullable
    fun bind(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        @Nullable source: ConfigurationPropertySource?
    ): Any?
}