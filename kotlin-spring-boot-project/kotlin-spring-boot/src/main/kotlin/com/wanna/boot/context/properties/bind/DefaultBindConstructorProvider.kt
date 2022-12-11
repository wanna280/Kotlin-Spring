package com.wanna.boot.context.properties.bind

import java.lang.reflect.Constructor

/**
 * [BindConstructorProvider]的默认实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 */
class DefaultBindConstructorProvider : BindConstructorProvider {

    override fun getBindConstructor(bindable: Bindable<*>, isNestedConstructorBinding: Boolean): Constructor<*>? {
        // TODO
        return null
    }
}