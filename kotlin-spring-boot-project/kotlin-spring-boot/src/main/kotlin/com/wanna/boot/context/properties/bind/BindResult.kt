package com.wanna.boot.context.properties.bind

import com.wanna.framework.lang.Nullable

/**
 * 绑定属性值的结果
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
class BindResult<T : Any>(@Nullable val value: T?) {

    companion object {

        /**
         * 没有绑定的成功的BindResult常量对象
         */
        @JvmField
        val UNBOUND = BindResult<Any>(null)

        /**
         * 根据绑定完成的Java实例对象, 去构建得到绑定结果BindResult
         *
         * @param value 绑定完成的实例对象
         * @return BindResult
         */
        @JvmStatic
        fun <T : Any> of(@Nullable value: T?): BindResult<T> {
            return BindResult(value)
        }
    }
}