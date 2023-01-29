package com.wanna.framework.simple.test.ex

import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.simple.test.tx.TxApp

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/20
 */
@Configuration(proxyBeanMethods = false)
class ExceptionTest {
}

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(ExceptionTest::class.java)
    applicationContext.getBean("exceptionTest", TxApp::class.java)
}