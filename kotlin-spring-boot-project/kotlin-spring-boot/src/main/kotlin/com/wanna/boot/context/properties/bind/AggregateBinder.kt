package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertySource

/**
 * 提供聚合的策略的绑定(例如Map/List/Array), 供[Binder]去进行内部使用
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
abstract class AggregateBinder<T : Any>(val context: Binder.Context) {

    /**
     * 是否允许去进行递归绑定
     *
     * @param source ConfigurationPropertySource
     * @return 如果允许递归绑定return true; 否则return false
     */
    protected fun isAllowRecursiveBinding(source: ConfigurationPropertySource): Boolean = false
}