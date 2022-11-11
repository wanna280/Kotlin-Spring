package com.wanna.boot

/**
 * SpringBoot的异常报告器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 */
interface SpringBootExceptionReporter {

    /**
     * 报告一个异常
     *
     * @param failure 要去进行报告的异常
     * @return 如果这个异常已经被报告了，那么return true
     */
    fun report(failure: Throwable): Boolean
}