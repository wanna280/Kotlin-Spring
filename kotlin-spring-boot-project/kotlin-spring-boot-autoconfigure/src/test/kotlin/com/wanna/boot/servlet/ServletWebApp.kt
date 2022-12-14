package com.wanna.boot.servlet

import com.wanna.boot.ApplicationType
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
@SpringBootApplication
class ServletWebApp

@RestController
class ServletController {

    @RequestMapping(["/servlet"])
    fun mapping(request: HttpServerRequest, response: HttpServerResponse): Any {
        return mapOf("servlet" to "servlet")
    }
}


fun main() {
    val applicationContext = runSpringApplication<ServletWebApp> {
        setApplicationType(ApplicationType.SERVLET)
    }
}