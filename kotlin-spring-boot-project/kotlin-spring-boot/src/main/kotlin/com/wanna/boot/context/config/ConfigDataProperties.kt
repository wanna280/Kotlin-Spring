package com.wanna.boot.context.config

import com.wanna.framework.lang.Nullable

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
class ConfigDataProperties(val imports: List<ConfigDataLocation>, @Nullable val activate: Activate? = null) {


    fun isActive(@Nullable activationContext: ConfigDataActivationContext?): Boolean {
        return activate == null || activate.isActive(activationContext)
    }


    class Activate {

        fun isActive(@Nullable activationContext: ConfigDataActivationContext?): Boolean {
            return false
        }
    }
}