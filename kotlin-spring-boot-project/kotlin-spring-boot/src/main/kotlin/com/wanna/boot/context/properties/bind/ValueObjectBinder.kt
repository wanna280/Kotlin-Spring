package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName

/**
 * 针对ValueObject的Binder
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 *
 * @param bindConstructorProvider 提供要去进行绑定的构造器的Provider
 */
open class ValueObjectBinder(private val bindConstructorProvider: BindConstructorProvider) : DataObjectBinder {

    override fun <T : Any> bind(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        context: Binder.Context,
        propertyBinder: DataObjectPropertyBinder
    ): T? {
        // TODO
        return null
    }

    override fun <T : Any> create(target: Bindable<T>, context: Binder.Context): T? {
        // TODO
        return null
    }
}