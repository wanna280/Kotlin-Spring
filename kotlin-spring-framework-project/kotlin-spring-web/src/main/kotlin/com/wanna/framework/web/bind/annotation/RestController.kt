package com.wanna.framework.web.bind.annotation

import com.wanna.framework.context.stereotype.Controller
import com.wanna.framework.core.annotation.AliasFor

/**
 * 标识这是一个Rest风格的Controller层的Spring Bean, 以ResponseBody的方式去进行返回数据
 * 这是一个组合注解, 通过去组合`@Controller`和`@ResponseBody`这两个注解去实现
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
    val value: String = ""
)
