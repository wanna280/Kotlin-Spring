package com.wanna.boot.autoconfigure

/**
 * 自动配置的Order
 *
 * @param value order
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
