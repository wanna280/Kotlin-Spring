package com.wanna.test.parameter

import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.beans.factory.ObjectProvider
import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.stereotype.Component

@ComponentScan(["com.wanna.test.parameter"])
@Configuration
open class ObjectProviderTest

@Component
class User

class U

@Component
class InjectUser {
    @Autowired
    var injectUser1: ObjectFactory<User>? = null

    @Autowired
    var injectUser2: ObjectProvider<User>? = null

    @Autowired
    var u: ObjectProvider<U>? = null
}

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(ObjectProviderTest::class.java)
    val injectUser = applicationContext.getBean(InjectUser::class.java)!!
    println(injectUser.injectUser1!!.getObject())
    println(injectUser.injectUser2!!.getObject())
    
    println(injectUser.u!!.getIfAvailable())

}