package com.wanna.framework.web.bind.annotation

import com.wanna.framework.context.stereotype.Controller
import com.wanna.framework.core.annotation.AliasFor

/**
 * 标识这是一个RestController，这是一个组合注解，组合了@Controller和@ResponseBody注解
 *
 * @see Controller
 * @see ResponseBody
 *
 * @param value beanName
 */
@Controller
@ResponseBody
annotation class RestController(
    @get:AliasFor(annotation = Controller::class, value = "value")
    val value: String = ""  // beanName
)
