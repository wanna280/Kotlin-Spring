package com.wanna.nacos.test.server.mapping

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.stereotype.Controller
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.method.annotation.RequestMapping
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.framework.web.ui.View

@Controller
class MyController {
    @RequestMapping(["/mav"])
    fun mav(): String {
        return "mav"
    }

    @RequestMapping(["/beanName"])
    fun beanName(): String {
        return "beanName"
    }
}

@ComponentScan(["com.wanna.nacos.test.server.mapping"])
@SpringBootApplication
class App {

    @Bean("beanName")
    fun mav(): View {
        return object : View {
            override fun render(model: Map<String, *>?, request: HttpServerRequest, response: HttpServerResponse) {
                val fis = ClassLoader.getSystemClassLoader().getResourceAsStream("templates/mav.html")
                val readAllBytes = fis!!.readAllBytes()
                response.getOutputStream().write(readAllBytes)
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
            }
        }
    }
}

fun main() {
    runSpringApplication<App>()
}