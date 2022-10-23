package com.wanna.framework.beans.factory.xml

import org.w3c.dom.Document
import org.xml.sax.EntityResolver
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource

/**
 * DocumentLoader，将一个文件去加载成为Document
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
interface DocumentLoader {
    fun loadDocument(
        inputSource: InputSource,
        entityResolver: EntityResolver?,
        errorHandler: ErrorHandler?,
        validationMode: Int,
        namespaceAware: Boolean
    ): Document
}