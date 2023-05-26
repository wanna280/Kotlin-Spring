package com.wanna.middleware.arthas.core.shell.cli

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
interface Completion {

    fun complete(candidates: List<String>)
}