package com.wanna.nacos.config.server.exception

import com.wanna.framework.dao.DataAccessException
import com.wanna.framework.web.bind.annotation.ControllerAdvice
import com.wanna.framework.web.bind.annotation.ExceptionHandler

/**
 * 全局的ExceptionHandler
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
@ControllerAdvice
open class GlobalExceptionHandler {

    /**
     * 处理不合法的参数异常
     *
     * @param ex 需要去进行处理的不合法参数异常
     */
    @ExceptionHandler(IllegalArgumentException::class)
    open fun handleIllegalArgumentException(ex: IllegalArgumentException): Any {
        return "IllegalArgumentException"
    }

    /**
     * 处理数据访问异常
     *
     * @param ex 需要去进行处理的数据访问异常
     */
    @ExceptionHandler(DataAccessException::class)
    open fun handleDataAccessException(ex: DataAccessException): Any {
        return "DataAccessException"
    }
}