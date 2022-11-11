package com.wanna.framework.web.bind.annotation

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.context.stereotype.Controller

/**
 * 标识它是一个[Controller]的增强的注解，Controller内部支持[ExceptionHandler]去进行配置局部的[ExceptionHandler]
 * 对于ControllerAdvice当中的配置，可以针对于所有的Controller都生效, 是一个全局的Controller的增强
 *
 * @see RestController
 * @see Controller
 *
 * @see InitBinder
 * @see ModelAttribute
 */
@Component
annotation class ControllerAdvice()
