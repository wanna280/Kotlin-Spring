package com.wanna.boot.origin

import com.wanna.framework.lang.Nullable

/**
 * 获取Origin的Provider
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/7
 */
fun interface OriginProvider {

    /**
     * 获取Origin的Provider方法
     *
     * @return origin
     */
    @Nullable
    fun getOrigin(): Origin?
}