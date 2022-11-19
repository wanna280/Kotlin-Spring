package com.wanna.test.web.server

import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.stereotype.Controller
import com.wanna.framework.web.DispatcherHandlerImpl
import com.wanna.framework.web.bind.WebDataBinder
import com.wanna.framework.web.bind.annotation.*
import com.wanna.framework.web.config.annotation.CorsRegistry
import com.wanna.framework.web.config.annotation.DelegatingWebMvcConfiguration
import com.wanna.framework.web.config.annotation.WebMvcConfigurer
import com.wanna.framework.web.context.support.AnnotationConfigWebApplicationContext
import com.wanna.framework.web.http.Cookie
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.framework.web.server.netty.server.support.NettyServer
import com.wanna.framework.web.server.netty.server.support.NettyServerHandler
import com.wanna.framework.web.ui.View
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@ComponentScan(["com.wanna.test.web.server"])
@Configuration(proxyBeanMethods = false)
open class WebServerTest {
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
                response.flush()
            }
        }
    }

    @Bean
    fun webMvcConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMapping(registry: CorsRegistry) {
                registry.addMapping("/**").allowedMethods("GET", "POST")
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
    @ModelAttribute
    fun model(): String {
        return "ma"
    }

    @InitBinder
    fun initBinder(webDataBinder: WebDataBinder) {
        println("ControllerAdvice-" + webDataBinder.getTarget())
    }
}

@RequestMapping(["/user"], method = [RequestMethod.GET, RequestMethod.POST])
@Controller
class MyController {

    companion object {
        private val logger = LoggerFactory.getLogger(MyController::class.java)
    }

    @ModelAttribute
    fun ma(): String {
        return "ma"
    }

    @RequestMapping(["/mav"])
    fun mav(@RequestParam("id") id: Int?, @RequestHeader("Content-Type") contentType: String?): String {
        println("id=$id, contentType=$contentType")
        return "mav"
    }

    @ResponseBody
    @GetMapping(["/{name}/beanName"])
    fun beanName(@PathVariable name: String, @PathVariable vars: Map<String, String>): Any {
        println("$name---$vars")
        return vars
    }

    @ModelAttribute
    @RequestMapping(["/request"])
    fun request(): User {
        return User()
    }

    @ResponseBody
    @RequestMapping(["/callable"])
    fun callable(): Callable<String> {
        logger.info("ThreadName=${Thread.currentThread().name}")
        return Callable<String> {
            logger.info("ThreadName=${Thread.currentThread().name}")
            "wanna"
        }
    }

    @ResponseBody
    @RequestMapping(["/produces"], produces = ["text/html"])
    fun produces(request: HttpServerRequest, response: HttpServerResponse): String {
        response.addCookie(Cookie("user", "wanna"))
        return "u"
    }

    @ResponseBody
    @RequestMapping(["/future"])
    fun async(): CompletableFuture<String> {
        return CompletableFuture.supplyAsync {
            TimeUnit.MILLISECONDS.sleep(5000L)
            "wanna"
        }
    }

    @ResponseBody
    @RequestMapping(["/asyncContext"])
    fun asyncContext(request: HttpServerRequest, response: HttpServerResponse) {
        val asyncContext = request.startAsync(request, response)
        CompletableFuture.runAsync {
            TimeUnit.MILLISECONDS.sleep(5000)
            asyncContext.getResponse()?.getOutputStream()?.write("wanna".toByteArray())
            asyncContext.getResponse()?.flush()
        }
    }
}

fun main() {
    val applicationContext = AnnotationConfigWebApplicationContext(WebServerTest::class.java)
    val nettyServer = NettyServer()
    nettyServer.setHandler(NettyServerHandler(applicationContext))
    nettyServer.start()
}