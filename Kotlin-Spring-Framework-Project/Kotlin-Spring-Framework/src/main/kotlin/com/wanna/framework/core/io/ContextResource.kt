package com.wanna.framework.core.io

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 */
interface ContextResource : Resource {
    fun getPathWithinContext(): String
}