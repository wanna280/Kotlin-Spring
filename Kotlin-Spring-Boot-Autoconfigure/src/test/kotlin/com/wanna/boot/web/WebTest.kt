package com.wanna.boot.web

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.processor.beans.internal.CommonAnnotationPostProcessor
import com.wanna.framework.core.util.AnnotationConfigUtils
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.method.annotation.RequestBody
import com.wanna.framework.web.method.annotation.RequestMapping
import com.wanna.framework.web.method.annotation.RequestParam
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import java.io.InputStream
import java.io.OutputStream
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.annotation.Resource

@SpringBootApplication
class WebTest

class User {
    val userId: String? = null
}

@RestController
class Controller : DisposableBean {

    @Autowired
    @com.wanna.framework.context.annotation.Lazy
    var processors: Map<String, out BeanPostProcessor>? = null

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

    @PostConstruct
    fun init() {
        println(this.processors)
        println("init")
    }

    @PreDestroy
    fun end() {
        println("destroy")
    }

    override fun destroy() {
        println("DisposableBean destroy")
    }
}

fun main(vararg args: String) {
    val applicationContext = runSpringApplication<WebTest>(*args)
}