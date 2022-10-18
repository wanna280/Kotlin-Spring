package com.wanna.test.xml

import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.ImportSource
import com.wanna.test.tx.TxApp
import com.wanna.test.xml.bean.User

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
@ImportSource(["classpath:context.xml"])
@Configuration(proxyBeanMethods = false)
class XmlTest

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(XmlTest::class.java)
    applicationContext.getBean(TxApp::class.java)

    println(applicationContext.getBean(User::class.java))
}