package com.wanna.boot.web

import com.wanna.boot.SpringApplication
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.method.annotation.RequestBody
import com.wanna.framework.web.method.annotation.RequestMapping
import com.wanna.framework.web.method.annotation.RequestParam
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import java.io.InputStream
import java.io.OutputStream

@SpringBootApplication
class WebTest {}

class User {
    val userId: String? = null
}

@RestController
class Controller {
    @RequestMapping(value = ["/user"])
    fun mapping(
        @RequestParam userId: String,
        request: HttpServerRequest,
        response: HttpServerResponse,
        @RequestBody(required = false) user: User?,
        inputStream: InputStream,
        outputStream: OutputStream
    ): User? {
        println(user)
        return user
    }
}

fun main(vararg args: String) {
    runSpringApplication<WebTest>(*args) {}
}