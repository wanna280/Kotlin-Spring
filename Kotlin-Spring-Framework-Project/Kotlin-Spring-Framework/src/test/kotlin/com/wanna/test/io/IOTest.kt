package com.wanna.test.io

import com.wanna.framework.core.io.DefaultResourceLoader

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 */
class IOTest {

}

fun main() {
    val resourceLoader = DefaultResourceLoader()
    val resource = resourceLoader.getResource("classpath:com/wanna/test/io/IOTestKt.class")
    val inputStream = resource.getInputStream()
    println(inputStream.readAllBytes())
}