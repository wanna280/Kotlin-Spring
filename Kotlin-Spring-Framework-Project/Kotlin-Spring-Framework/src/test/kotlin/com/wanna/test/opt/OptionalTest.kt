package com.wanna.test.opt

import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.stereotype.Component
import java.util.Optional

@Configuration(proxyBeanMethods = false)
@ComponentScan(["com.wanna.test.opt"])
class OptionalTest

@Configuration(proxyBeanMethods = false)
class Config {

    @Autowired
    var user: Optional<User>? = null
}

@Component
class User

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(OptionalTest::class.java)
    val bean = applicationContext.getBean(Config::class.java)
    println(bean.user)
}