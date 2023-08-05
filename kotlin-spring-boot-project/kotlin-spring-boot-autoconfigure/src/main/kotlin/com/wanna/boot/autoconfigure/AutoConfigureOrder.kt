package com.wanna.boot.autoconfigure

/**
 * 指定自动配置的Order
 *
 * @param value order, 数字越小优先级越高
 *
 * @see AutoConfigurationSorter
 * @see AutoConfigureBefore
 * @see AutoConfigureAfter
 */
annotation class AutoConfigureOrder(val value: Int = DEFAULT_ORDER) {
    companion object {
        /**
         * 默认的Order
         */
        const val DEFAULT_ORDER = 0
    }
}
