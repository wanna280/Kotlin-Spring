package com.wanna.framework.beans.factory.xml

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.AbstractBeanDefinitionReader
import com.wanna.framework.core.io.Resource
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.xml.SimpleSaxErrorHandler
import org.w3c.dom.Document
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource

/**
 * Xml的BeanDefinitionReader
 *
 * @param registry BeanDefinitionRegistry
 */
open class XmlBeanDefinitionReader(registry: BeanDefinitionRegistry) : AbstractBeanDefinitionReader(registry) {

    /**
     * NamespaceHandler的解析器
     */
    @Nullable
    private var namespaceHandlerResolver: NamespaceHandlerResolver? = null

    /**
     * BeanDefinition的DocumentReader的类
     */
    private var beanDefinitionDocumentReaderClass: Class<out BeanDefinitionDocumentReader> =
        DefaultBeanDefinitionDocumentReader::class.java

    /**
     * DocumentLoader，提供将资源文件加载成为W3C的Document
     */
    private var documentLoader: DocumentLoader = DefaultDocumentLoader()

    /**
     * ErrorHandler，为Document的加载准备的
     */
    private var errorHandler = SimpleSaxErrorHandler(logger)

    @Nullable
    private var entityResolver: EntityResolver? = null

    private var namespaceAware = false

    /**
     * 根据一个资源去加载BeanDefinition到BeanDefinitionRegistry当中
     *
     * @param resource 要去进行加载的资源
     * @return 通过该资源加载到的BeanDefinition的数量
     */
    override fun loadBeanDefinitions(resource: Resource): Int =
        resource.getInputStream().use { return doLoadBeanDefinitions(InputSource(it), resource) }

    /**
     * 将给定的资源还有DocumentLoader去解析成为W3C的Document对象，
     * 并交给BeanDefinitionDocumentReader去进行BeanDefinition的解析
     *
     * @param inputSource InputSource
     * @param resource Resource
     * @return 从资源当中去加载到的BeanDefinition数量
     */
    protected open fun doLoadBeanDefinitions(inputSource: InputSource, resource: Resource): Int =
        registerBeanDefinitions(doLoadDocument(inputSource, resource), resource)

    /**
     * 使用BeanDefinitionDocumentReader去将Document去转换成为BeanDefinition
     *
     * @param document W3C的Document
     * @param resource 要去进行加载的资源
     * @return 从给定的资源当中使用DocumentReader去加载到的BeanDefinition的数量
     */
    open fun registerBeanDefinitions(document: Document, resource: Resource): Int {
        // 1.先记录一下加载资源之前的BeanDefinition数量
        val beanDefinitionCount = getRegistry().getBeanDefinitionCount()

        // 2.创建BeanDefinitionDocumentReader
        val beanDefinitionDocumentReader = createBeanDefinitionDocumentReader()

        // 3.利用BeanDefinitionDocumentReader去注册BeanDefinition
        beanDefinitionDocumentReader.registerBeanDefinitions(document, createReaderContext(resource))

        // 4.利用加载之后的BeanDefinition和加载之前的BeanDefinition的数量做对比，得到本次加载的BeanDefinition的数量
        return getRegistry().getBeanDefinitionCount() - beanDefinitionCount
    }

    /**
     * 创建ReaderContext，将一些需要用到的对象去包装到XmlReaderContext当中
     *
     * @param resource 资源对象
     * @return 创建号的XmlReaderContext
     */
    open fun createReaderContext(resource: Resource): XmlReaderContext {
        return XmlReaderContext(this, getNamespaceHandlerResolver(), resource)
    }

    /**
     * 创建BeanDefinitionDocumentReader
     *
     * @return BeanDefinitionDocumentReader
     * @throws NoSuchMethodException 找不到无参数构造器的话
     * @throws IllegalAccessException 访问无参数构造器失败
     */
    @Throws(NoSuchMethodException::class, IllegalAccessException::class)
    open fun createBeanDefinitionDocumentReader(): BeanDefinitionDocumentReader =
        BeanUtils.instantiateClass(beanDefinitionDocumentReaderClass)

    /**
     * 根据资源使用DocumentLoader去加载到W3C的Document对象
     *
     * @param inputSource InputSource
     * @param resource 资源文件
     * @return 加载到的W3C的Document对象
     */
    protected open fun doLoadDocument(inputSource: InputSource, resource: Resource): Document =
        documentLoader.loadDocument(inputSource, getEntityResolver(), errorHandler, 0, namespaceAware)

    /**
     * 获取EntityResolver(提供".xsd"和".dtd"文件的解析)
     *
     * @return EntityResolver
     */
    open fun getEntityResolver(): EntityResolver? {
        return this.entityResolver
    }

    /**
     * 获取NamespaceHandlerResolver，如果没有指定的话，那么我们去创建默认的
     *
     * @return NamespaceHandlerResolver
     */
    open fun getNamespaceHandlerResolver(): NamespaceHandlerResolver {
        if (this.namespaceHandlerResolver == null) {
            this.namespaceHandlerResolver = createNamespaceHandlerResolver()
        }
        return this.namespaceHandlerResolver ?: throw IllegalStateException("NamespaceHandlerResolver不能为空")
    }

    /**
     * 创建NamespaceHandler的Resolver
     *
     * @return NamespaceHandlerResolver
     */
    open fun createNamespaceHandlerResolver(): NamespaceHandlerResolver {
        val classLoaderToUse =
            if (getResourceLoader() != null) getResourceLoader()?.getClassLoader() else getBeanClassLoader()
        return DefaultNamespaceHandlerResolver(classLoaderToUse)
    }


    /**
     * 设置自定义的NamespaceHandlerResolver
     *
     * @param namespaceHandlerResolver 想要使用的NamespaceHandlerResolver
     */
    open fun setNamespaceHandlerResolver(namespaceHandlerResolver: NamespaceHandlerResolver?) {
        this.namespaceHandlerResolver = namespaceHandlerResolver
    }

    /**
     * 设置BeanDefinitionDocumentReader的类
     *
     * @param readerClass 你想要使用的BeanDefinitionDocumentReader的类
     */
    open fun setBeanDefinitionDocumentReaderClass(readerClass: Class<out BeanDefinitionDocumentReader>) {
        this.beanDefinitionDocumentReaderClass = readerClass
    }

    /**
     * 设置DocumentLoader，提供W3C的Document的加载功能
     *
     * @param documentLoader 你想要使用的DocumentLoader
     */
    open fun setDocumentLoader(documentLoader: DocumentLoader) {
        this.documentLoader = documentLoader
    }
}