package com.wanna.framework.test.junit

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.test.JUnitApp
import com.wanna.framework.test.context.ContextConfiguration
import com.wanna.framework.test.context.junit.jupiter.SpringExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
@ContextConfiguration(classes = [JUnitApp::class])
@ExtendWith(SpringExtension::class)
class JUnitTest {

    @Autowired
    private var list: Map<String, BeanPostProcessor>? = null

    @Autowired
    private var app: JUnitApp? = null

    @Test
    fun test() {
        println(list)
    }
}