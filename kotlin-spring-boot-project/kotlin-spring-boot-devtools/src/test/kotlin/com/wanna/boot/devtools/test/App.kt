package com.wanna.boot.devtools.test

import com.wanna.boot.devtools.RemoteSpringApplication

class App {
    fun test() {
        println("666")
    }
}

fun main(vararg args: String) {
    RemoteSpringApplication.main("http://localhost:9966")
}