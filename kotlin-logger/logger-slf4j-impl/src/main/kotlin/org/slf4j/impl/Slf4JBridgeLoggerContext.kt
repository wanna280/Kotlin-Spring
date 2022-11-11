package org.slf4j.impl

import com.wanna.logger.impl.AbstractLoggerContext
import com.wanna.logger.impl.appender.support.ConsoleLoggerAppender
import org.slf4j.ILoggerFactory

/**
 * 这是一个桥接Slf的ILoggerFactory，这里我们需要做的是对父类当中的一些默认属性去进行扩展；
 * 我们必须实现newLogger方法：用于告诉LoggerImpl的实现方，应该使用何种方式去进行Logger的创建，全局创建Logger都会走这个方法去进行创建；
 * 因此要对Logger去进行干预，完全可以在这里去对Logger去进行自定义
 *
 * @see com.wanna.logger.impl.AbstractLoggerContext
 * @see Slf4jBridgeLogcLogger
 */
open class Slf4JBridgeLoggerContext : ILoggerFactory, AbstractLoggerContext<Slf4jBridgeLogcLogger>() {
    override fun newLogger(name: String): Slf4jBridgeLogcLogger {
        return Slf4jBridgeLogcLogger(name)
    }
}