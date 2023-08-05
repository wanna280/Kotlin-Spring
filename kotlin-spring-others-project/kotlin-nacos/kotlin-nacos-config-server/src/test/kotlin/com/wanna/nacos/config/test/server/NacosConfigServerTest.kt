package com.wanna.nacos.config.test.server

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.context.annotation.ComponentScan

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/16
 */
@SpringBootApplication
@ComponentScan(["com.google"])
class AppTest {

}

fun main() {
    runSpringApplication<AppTest>()
}