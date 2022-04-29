package com.wanna.test.env

import com.wanna.framework.core.environment.MapPropertySource
import com.wanna.framework.core.environment.SimpleCommandLinePropertySource
import com.wanna.framework.core.environment.StandardEnvironment

fun main(vararg args: String) {
    val environment = StandardEnvironment()
    val map: MutableMap<String, String> = java.util.HashMap()
    map["user.name0"] = "666"
    map["wanna"] = "%{user.name0}"
    map["wanna2"] = "%{wanna}"
    environment.getPropertySources().addLast(MapPropertySource("map", map))
    environment.getPropertySources().addLast(SimpleCommandLinePropertySource(*args))

    val property = environment.resolveRequiredPlaceholders("%{%{wanna}} %{wanna} %{wanna2} %{wanna2}")
    println(property)
}