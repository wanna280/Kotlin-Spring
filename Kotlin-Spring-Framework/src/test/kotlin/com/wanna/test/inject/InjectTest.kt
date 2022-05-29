package com.wanna.test.inject

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.*
import com.wanna.framework.context.stereotype.Component

@ComponentScan(["com.wanna.test.inject"])
@Configuration
class Main

@Scope(BeanDefinition.SCOPE_PRTOTYPE)
@Component
class User1 {

    @Autowired
    var user2: User2? = null

}

@Scope(BeanDefinition.SCOPE_PRTOTYPE)
@Component
class User2 {
    @Autowired
    var user1: User1? = null
}

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(Main::class.java)
    val user1 = applicationContext.getBean("user1")
}