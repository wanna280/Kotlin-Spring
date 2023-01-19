package com.wanna.framework.util.xml

import com.wanna.common.logging.Logger
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
class SimpleSaxErrorHandler(private val logger: Logger) : ErrorHandler {
    override fun warning(exception: SAXParseException?) {

    }

    override fun error(exception: SAXParseException?) {

    }

    override fun fatalError(exception: SAXParseException?) {

    }
}