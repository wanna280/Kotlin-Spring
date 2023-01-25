package com.wanna.framework.simple.test.mediatype

import com.wanna.framework.util.MimeTypeUtils

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/25
 */
class MediaTypeTest {
}

fun main() {
    val mimeTypes = "a,\"b\",c"
    println(com.wanna.framework.util.MimeTypeUtils.tokenize(mimeTypes))
    println(MimeTypeUtils.tokenize(mimeTypes))

}