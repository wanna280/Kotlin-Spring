package com.wanna.framework.aop.cglib

import net.sf.cglib.core.DefaultNamingPolicy

/**
 * 设置自定义的命名策略
 */
class SpringNamingPolicy : DefaultNamingPolicy() {
    companion object {
        @JvmField
        val INSTANCE = SpringNamingPolicy()
    }

    override fun getTag() = "ByWannaSpringCGLIB"
}