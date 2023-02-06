package com.wanna.framework.core.cglib.core

import net.sf.cglib.core.DefaultNamingPolicy

/**
 * 设置Spring自定义的命名策略, 设置TAG为"ByWannaSpringCGLIB"
 *
 * @see DefaultNamingPolicy
 */
object SpringNamingPolicy : DefaultNamingPolicy() {
    override fun getTag() = "ByWannaSpringCGLIB"
}