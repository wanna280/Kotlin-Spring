package com.wanna.boot.devtools.restart

import com.wanna.framework.context.annotation.Scope

/**
 * 标注这个注解的Bean, 将会被注册到RestartScope当中
 *
 * @see RestartScopeInitializer.RestartScope
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Scope("restart")
annotation class RestartScope
