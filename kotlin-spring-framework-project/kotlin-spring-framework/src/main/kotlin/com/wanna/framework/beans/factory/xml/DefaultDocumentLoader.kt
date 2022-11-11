package com.wanna.framework.beans.factory.xml

import org.w3c.dom.Document
import org.xml.sax.EntityResolver
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * DocumentLoader的默认实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class DefaultDocumentLoader : DocumentLoader {

    /**
     * 将给定的资源去加载成为Document
     *
     * @param inputSource InputSource(资源文件)
     * @param entityResolver EntityResolver
     * @param errorHandler ErrorHandler
     * @param validationMode validationMode
     * @param namespaceAware namespaceAware
     */
    override fun loadDocument(
        inputSource: InputSource,
        entityResolver: EntityResolver?,
        errorHandler: ErrorHandler?,
        validationMode: Int,
        namespaceAware: Boolean
    ): Document {
        val documentBuilderFactory = createDocumentBuilderFactory(validationMode, namespaceAware)
        val documentBuilder = createDocumentBuilder(documentBuilderFactory, errorHandler, entityResolver)
        return documentBuilder.parse(inputSource)
    }

    protected open fun createDocumentBuilder(
        documentBuilderFactory: DocumentBuilderFactory,
        errorHandler: ErrorHandler?,
        entityResolver: EntityResolver?
    ): DocumentBuilder {
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        if (entityResolver != null) {
            documentBuilder.setEntityResolver(entityResolver)
        }
        if (errorHandler != null) {
            documentBuilder.setErrorHandler(errorHandler)
        }
        return documentBuilder
    }

    protected open fun createDocumentBuilderFactory(
        validationMode: Int,
        namespaceAware: Boolean
    ): DocumentBuilderFactory {
        val documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance()
        documentBuilderFactory.isNamespaceAware = namespaceAware
        return documentBuilderFactory
    }
}