package com.wanna.framework.simple.test.annotation

import com.wanna.framework.context.stereotype.Service
import com.wanna.framework.core.annotation.MergedAnnotations

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/16
 */
@Service
class AliasForTest {
}

fun main() {

    val mergedAnnotations = MergedAnnotations.from(AliasForTest::class.java)
    val mergedAnnotation = mergedAnnotations.get(Service::class.java)
    println(mergedAnnotations)
}