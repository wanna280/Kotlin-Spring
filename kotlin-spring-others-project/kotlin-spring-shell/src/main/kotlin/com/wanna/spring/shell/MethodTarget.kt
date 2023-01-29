package com.wanna.spring.shell

import java.lang.reflect.Method

/**
 * MethodTarget
 *
 * @param method 目标方法
 * @param bean 目标对象
 * @param help help信息
 */
open class MethodTarget(val method: Method, val bean: Any, val help: Command.Help) : Command