package com.wanna.boot

/**
 * ExitCode的生成器，用于去生成整个Java应用退出的ExitCode
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 */
interface ExitCodeGenerator {

    /**
     * 获取ExitCode
     *
     * @return ExitCode
     */
    fun getExitCode(): Int
}