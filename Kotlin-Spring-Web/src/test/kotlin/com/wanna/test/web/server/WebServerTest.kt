package com.wanna.test.web.server

import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.stereotype.Controller
import com.wanna.framework.web.DispatcherHandlerImpl
import com.wanna.framework.web.bind.WebDataBinder
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.config.annotation.DelegatingWebMvcConfiguration
import com.wanna.framework.web.config.annotation.WebMvcConfigurationSupport
import com.wanna.framework.web.context.support.AnnotationConfigWebApplicationContext
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.method.annotation.*
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.framework.web.server.netty.server.support.NettyServer
import com.wanna.framework.web.server.netty.server.support.NettyServerHandler
import com.wanna.framework.web.ui.View

@ComponentScan(["com.wanna.test.web.server"])
@Configuration(proxyBeanMethods = false)
class WebServerTest {
    @Configuration(proxyBeanMethods = false)
    class WebMvcConfig : DelegatingWebMvcConfiguration()

    @Bean
    fun dispatcherHandler(): DispatcherHandlerImpl {
        return DispatcherHandlerImpl()
    }

    @Bean
    fun listener(dispatcherHandler: DispatcherHandlerImpl): ApplicationListener<*> {
        return dispatcherHandler.ContextRefreshListener()
    }

    @Bean("beanName")
    fun mav(): View {
        return object : View {
            override fun render(model: Map<String, *>?, request: HttpServerRequest, response: HttpServerResponse) {
                val mavViewUrl = "templates/mav.html"
                val fis = ClassLoader.getSystemClassLoader().getResourceAsStream(mavViewUrl)
                val readAllBytes = fis?.readAllBytes() ?: throw IllegalStateException("无法找到指定的资源[$mavViewUrl]")
                response.getOutputStream().write(readAllBytes)
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
            }
        }
    }
}

class User {
    var id: Int = 0
    var name: String = ""
    override fun toString() = "User(id=$id, name='$name')"
}

@RestController
class MyModelController {

    @InitBinder
    fun initBinder(webDataBinder: WebDataBinder) {
        println("local-" + webDataBinder.getObjectName())
    }

    @RequestMapping(["/model"])
    fun model(user: User): Any {
        println("" + user.id + "---")
        return user
    }
}

@ControllerAdvice
class MyControllerAdvice {
    @InitBinder
    fun initBinder(webDataBinder: WebDataBinder) {
        println("ControllerAdvice-" + webDataBinder.getTarget())
    }
}

@Controller
class MyController {
    @RequestMapping(["/mav"])
    fun mav(@RequestParam("id") id: Int?, @RequestHeader("Content-Type") contentType: String?): String {
        println("id=$id, contentType=$contentType")
        return "mav"
    }

    @RequestMapping(["/beanName"])
    fun beanName(): String {
        return "beanName"
    }
}

fun main() {
    val applicationContext = AnnotationConfigWebApplicationContext(WebServerTest::class.java)
    val nettyServer = NettyServer()
    nettyServer.setHandler(NettyServerHandler(applicationContext))
    nettyServer.start()
}