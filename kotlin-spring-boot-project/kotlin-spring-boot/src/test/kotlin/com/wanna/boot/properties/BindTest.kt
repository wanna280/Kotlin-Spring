package com.wanna.boot.properties

import com.wanna.boot.SpringApplication
import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.framework.core.environment.PropertiesPropertySource
import com.wanna.framework.core.environment.StandardEnvironment

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
class BindTest {

    var name: String = ""
}

class A {
    var b: B? = null
}

class B {
    var c: C? = null
}

class C {
    var d: D? = null
}

class D {
    var name: Boolean? = null
}

fun main() {
    val environment = StandardEnvironment()
    environment.getPropertySources()
        .addLast(
            PropertiesPropertySource(
                "propertySource",
                mapOf("a.b.c.d.name" to "false")
            )
        )

    val a = A()
    val bindResult = Binder.get(environment)
        .bind("a", Bindable.ofInstance(a))

    println(bindResult)
}