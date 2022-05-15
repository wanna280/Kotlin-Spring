package com.wanna.boot.web

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.stereotype.Component
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

@SpringBootApplication
class WebTest {

    @Bean
    fun u2(): User {
        return User()
    }
}

@Qualifier("u")
@Component
class User {
    val userId: String? = null
}

@RestController
class Controller : DisposableBean {

    @Autowired
    @Qualifier("u")
    var u: User? = null

    @Autowired
    @Qualifier("u2")
    var u2: User? = null

    @Autowired
    @Qualifier(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)
    var processors: BeanPostProcessor? = null

    @RequestMapping(value = ["/user"])
    fun mapping(
        @RequestParam userId: String,
        request: HttpServerRequest,
        response: HttpServerResponse,
        @RequestBody(required = false) user: User?,
        inputStream: InputStream,
        outputStream: OutputStream
    ): Any? {
        println(user)
        return userId
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
    println(applicationContext)
}