package com.wanna.framework.core

import com.wanna.framework.lang.Nullable

/**
 * 获取到Spring的版本信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/24
 */
object SpringVersion {

    /**
     * 获取到Spring的版本信息, 如果无法获取到return null
     *
     * @return version
     */
    @JvmStatic
    @Nullable
    fun getVersion(): String? {
        val pkg = SpringVersion::class.java.`package`
        return pkg?.implementationVersion
    }

}