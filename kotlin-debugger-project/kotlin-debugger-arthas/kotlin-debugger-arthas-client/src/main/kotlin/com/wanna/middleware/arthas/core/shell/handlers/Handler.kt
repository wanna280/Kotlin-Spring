package com.wanna.middleware.arthas.core.shell.handlers

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
fun interface Handler<E : Any> {

    fun handle(event: E)
}