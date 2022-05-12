package org.slf4j.impl

import com.wanna.logger.impl.ContextInitializer
import com.wanna.logger.impl.AbstractLoggerContext

/**
 * 这是一个Slf4j的上下文初始化器的桥接类，目的是可以自己扩展原来的Logger的上下文初始化方法，为扩展自己原来的Logger提供支持；
 * 方便后期去修改LoggerImpl的默认配置信息，因此这个类去扩展原来的ContextInitializer
 *
 * @see ContextInitializer
 */
open class Slf4jBridgeContextInitializer(abstractLoggerContext: Slf4JBridgeLoggerContext) :
    ContextInitializer<Slf4jBridgeLogcLogger>(abstractLoggerContext)