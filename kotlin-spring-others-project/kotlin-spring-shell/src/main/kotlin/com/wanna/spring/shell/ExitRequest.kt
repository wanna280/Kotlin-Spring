package com.wanna.spring.shell

/**
 * 退出请求
 *
 * @param code code
 */
class ExitRequest(val code: Int = 0) : java.lang.RuntimeException()