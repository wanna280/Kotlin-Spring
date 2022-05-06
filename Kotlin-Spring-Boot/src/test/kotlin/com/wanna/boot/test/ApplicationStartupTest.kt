package com.wanna.boot.test

import com.wanna.boot.ApplicationType
import com.wanna.boot.SpringApplication
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.core.metrics.DefaultApplicationStartup
import com.wanna.framework.core.metrics.StartupStep
import org.slf4j.LoggerFactory
import java.util.function.Supplier

/**
 * 用于对ApplicationStartup去进行功能设置，对过程当中产生的数据去以日志的形式去进行输出
 */
@Configuration
class ApplicationStartupTest

class MyApplicationStartup : ApplicationStartup {
    companion object {
        private val logger = LoggerFactory.getLogger(MyStartupStep::class.java)
    }

    override fun start(name: String): StartupStep {
        return MyStartupStep(name)
    }

    class MyStartupStep(private val stepName: String) : DefaultApplicationStartup.DefaultStep() {
        override fun tag(key: String, value: String): StartupStep {
            logger.info("stepName:$stepName, tag:$key --- $value")
            return super.tag(key, value)
        }

        override fun tag(key: String, value: Supplier<String>): StartupStep {
            logger.info("stepName:$stepName, tag:$key --- ${value.get()}")
            return super.tag(key, value)
        }
    }
}

fun main(vararg args: String) {
    val springApplication = SpringApplication(ApplicationStartupTest::class.java)
    springApplication.setApplicationStartup(MyApplicationStartup())
    springApplication.setApplicationType(ApplicationType.NONE)
    springApplication.run(*args)
}