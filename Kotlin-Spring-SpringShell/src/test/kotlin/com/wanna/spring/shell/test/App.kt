package com.wanna.spring.shell.test

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.spring.shell.annotation.ShellComponent
import com.wanna.spring.shell.annotation.ShellMethod

@SpringBootApplication
open class App

@ShellComponent
open class MyShellComponent {
    @ShellMethod(value = "Int类型的加法")
    open fun addInt(left: Int, right: Int): Int {
        return left + right
    }

    @ShellMethod(value = "String类型的加法")
    open fun addStr(left: String, right: String): String {
        return left + right
    }
}

fun main() {
    val applicationContext = runSpringApplication<App>()
}