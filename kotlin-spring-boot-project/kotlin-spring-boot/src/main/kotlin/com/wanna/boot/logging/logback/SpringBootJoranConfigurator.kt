package com.wanna.boot.logging.logback

import ch.qos.logback.classic.joran.JoranConfigurator
import com.wanna.boot.logging.LoggingInitializationContext

/**
 * 对于Logback的[JoranConfigurator]去提供扩展规则, TODO, 这块Logback的内容不太了解
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/22
 */
internal class SpringBootJoranConfigurator(val initializationContext: LoggingInitializationContext) :
    JoranConfigurator() {

}