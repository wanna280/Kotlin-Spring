package com.wanna.framework.aop

import com.wanna.framework.lang.Nullable

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/8
 */
interface TargetClassAware {

    @Nullable
    fun getsTargetClass(): Class<*>?
}