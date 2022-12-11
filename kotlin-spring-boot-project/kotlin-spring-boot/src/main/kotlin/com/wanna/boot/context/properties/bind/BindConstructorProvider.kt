package com.wanna.boot.context.properties.bind

import java.lang.reflect.Constructor

/**
 * 提供构造器绑定的Provider
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 */
fun interface BindConstructorProvider {

    companion object {
        /**
         * [BindConstructorProvider]的默认实现的单例对象
         */
        @JvmStatic
        val DEFAULT = DefaultBindConstructorProvider()
    }

    /**
     * 获取到需要用于去进行绑定的构造器
     *
     * @param bindable Bindable
     * @return 获取到的需要去进行绑定的构造器(无法找到的话return null)
     */
    fun getBindConstructor(bindable: Bindable<*>, isNestedConstructorBinding: Boolean): Constructor<*>?
}