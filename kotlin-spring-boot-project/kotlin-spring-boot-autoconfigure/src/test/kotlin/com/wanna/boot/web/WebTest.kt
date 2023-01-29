package com.wanna.boot.web

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.format.FormatterRegistry
import com.wanna.framework.beans.factory.config.BeanPostProcessor
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.convert.converter.Converter
import com.wanna.framework.context.annotation.AnnotationConfigUtils
import com.wanna.framework.web.HandlerInterceptor
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.config.annotation.InterceptorRegistry
import com.wanna.framework.web.config.annotation.WebMvcConfigurer
import com.wanna.framework.web.bind.annotation.RequestBody
import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.bind.annotation.RequestParam
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

    @Bean
    fun webMvcConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addInterceptors(registry: InterceptorRegistry) {
                registry.addInterceptor(object : HandlerInterceptor {
                    override fun preHandle(
                        request: HttpServerRequest,
                        response: HttpServerResponse,
                        handler: Any
                    ): Boolean {
                        println("pre")
                        return true
                    }

                    override fun postHandle(request: HttpServerRequest, response: HttpServerResponse, handler: Any) {
                        println("post")
                    }

                    override fun afterCompletion(
                        request: HttpServerRequest,
                        response: HttpServerResponse,
                        handler: Any,
                        ex: Throwable?
                    ) {
                        println("after")
                    }
                })
            }

            override fun addFormatters(formatterRegistry: FormatterRegistry) {
                formatterRegistry.addConverter(MyConverter())
            }
        }
    }
}

class MyConverter : Converter<Int, String> {
    override fun convert(source: Int?): String? {
        return source?.toString()
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
    val applicationContext = runSpringApplication<WebTest>("--server.port=8089")
    println(applicationContext)
}