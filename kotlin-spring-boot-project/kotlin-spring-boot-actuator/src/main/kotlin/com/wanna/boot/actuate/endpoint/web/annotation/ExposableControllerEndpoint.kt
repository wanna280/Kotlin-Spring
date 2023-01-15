package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.boot.actuate.endpoint.ExposableEndpoint
import com.wanna.boot.actuate.endpoint.Operation
import com.wanna.boot.actuate.endpoint.web.PathMappedEndpoint

/**
 * 可以暴露的ControllerEndpoint, 对于每个ControllerEndpoint而言, 都应该可以去获取到Controller对象
 *
 * @see ExposableEndpoint
 * @see PathMappedEndpoint
 */
interface ExposableControllerEndpoint : ExposableEndpoint<Operation>, PathMappedEndpoint {

    /**
     * 获取该ControllerEndpoint的Controller对象
     *
     * @return ControllerObject
     */
    fun getController(): Any
}