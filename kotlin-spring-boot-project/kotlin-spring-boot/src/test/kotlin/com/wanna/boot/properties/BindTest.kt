package com.wanna.boot.properties

import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.framework.core.environment.MapPropertySource
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
    var flag: List<Boolean>? = null
}

class E {
    var name: Map<String, F>? = null

    var values: Map<String, List<String>>? = null

    var values2: Map<String, Map<String, String>>? = null

    /**
     * 这种情况? 怎么办? TODO
     */
    var values3: Map<String, *>? = null
}

class F {
    var list: List<String>? = null
}

fun main() {
    val environment = StandardEnvironment()
    environment.getPropertySources()
        .addLast(
            PropertiesPropertySource(
                "propertySource",
                mapOf(
                    "a.b.c.d.flag" to "false,false,true",
                    "a.b.c.d.flag1[0]" to "false",
                    "a.b.c.d.flag1[1]" to "true",
                    "a.b.c.d.flag1[2]" to "false",
                    "e.name.list.list[0]" to "wanna",
                    "e.name.list.list[1]" to "wanna2",
                    "e.values.wanna.wanna[0]" to "wanna",
                    "e.values.wanna.wanna[1]" to "wanna2",
                    "e.values.wanna.wanna[2]" to "wanna3",
                    "e.values2.wanna.wanna111" to "wanna",
                    "e.values2.wanna.wanna222" to "wanna2",
                    "e.values2.wanna.wanna333" to "wanna3",
                    "e.values3.wanna666.wanna777" to "wanna",
                    "e.values3.wanna6666.wanna7777" to "wanna2",
                    "e.values3.wanna.wanna.xxx" to "wanna3",
                    "e.values3.wanna.wanna.yyy" to "wanna3"
                )
            )
        )

    val a = A()
    val bindResult = Binder.get(environment)
        .bind("a", Bindable.ofInstance(a))

    println(bindResult)

    val e = E()

    val result = Binder.get(environment).bind("e", Bindable.ofInstance(e))

    println(result)
}