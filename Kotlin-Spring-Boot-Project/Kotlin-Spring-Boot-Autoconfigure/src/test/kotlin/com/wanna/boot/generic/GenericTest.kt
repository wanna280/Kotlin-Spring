package com.wanna.boot.generic

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.stereotype.Component

@SpringBootApplication
class GenericTest {

    @Autowired
    var vs: Map<String, V>? = null

    @Autowired
    var v1: V? = null

}

@Component
class V

interface U<T, E>

interface U2<S : User<Any>>

open class User<T> : U<Array<T>, GenericTest>, U2<User<Any>> {

}

fun main() {
    val applicationContext = runSpringApplication<GenericTest>()
    val genericTest = applicationContext.getBean(GenericTest::class.java)
    println(genericTest)
}