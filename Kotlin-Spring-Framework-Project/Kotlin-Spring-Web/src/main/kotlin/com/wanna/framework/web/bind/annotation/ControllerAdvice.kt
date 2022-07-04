package com.wanna.framework.web.bind.annotation

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.context.stereotype.Controller

/**
 * 标识它是一个Controller的增强的注解，Controller内部支持@ExceptionHandler，
 * ControllerAdvice当中的配置，可以针对于所有的Controller生效
 *
 * @see RestController
 * @see Controller
 */
@Component
annotation class ControllerAdvice()
