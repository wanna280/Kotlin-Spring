package com.wanna.framework.beans.factory.xml

import com.wanna.framework.beans.factory.support.BeanDefinitionReaderUtils
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.support.ResourcePatternUtils
import com.wanna.framework.util.ResourceUtils
import com.wanna.framework.util.StringUtils
import com.wanna.common.logging.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.net.URISyntaxException

/**
 * 默认的BeanDefinitionDocumentReader的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class DefaultBeanDefinitionDocumentReader : BeanDefinitionDocumentReader {
    companion object {

        /**
         * bean标签
         */
        const val BEAN_ELEMENT = "bean"

        /**
         * 嵌套的beans标签, Spring的XML配置文件允许beans标签内部嵌套beans标签
         */
        const val NESTED_BEANS_ELEMENT = "beans"

        /**
         * 标签的name属性
         */
        const val NAME_ATTRIBUTE = "name"

        /**
         * import标签
         */
        const val IMPORT_ELEMENT = "import"

        /**
         * resource属性
         */
        const val RESOURCE_ATTRIBUTE = "resource"

        /**
         * profile属性
         */
        const val PROFILE_ATTRIBUTE = "profile"

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(DefaultBeanDefinitionDocumentReader::class.java)
    }

    /**
     * XmlReader的上下文信息
     */
    private var readerContext: XmlReaderContext? = null

    /**
     * BeanDefinitionParser的Delegate
     */
    private var delegate: BeanDefinitionParserDelegate? = null

    /**
     * 获取ReaderContext
     *
     * @return ReaderContext
     * @throws IllegalStateException 如果ReaderContext没有被初始化
     */
    protected open fun getReaderContext(): XmlReaderContext =
        readerContext ?: throw IllegalStateException("ReaderContext不能为空")

    /**
     * 将W3C的Document解析成为BeanDefinition, 并完成注册功能
     *
     * @param document W3C的Document
     * @param readerContext 上下文信息(NamespaceHandlerResolver/Resource/XmlBeanDefinitionReader...)
     */
    override fun registerBeanDefinitions(document: Document, readerContext: XmlReaderContext) {

        // 先将ReaderContext去保存起来
        this.readerContext = readerContext

        // 获取到Document的Root元素, 去进行真正的BeanDefinition的注册
        doRegisterBeanDefinitions(document.documentElement)
    }

    /**
     * 真正地去注册BeanDefinition
     *
     * @param root Spring的XML配置文件的根元素
     */
    protected open fun doRegisterBeanDefinitions(root: Element) {
        // 暂时创建一个新的delegate替换掉this.delegate, 在解析完成之后, 将delegate去进行恢复
        val parent = this.delegate
        val delegate = createDelegate(getReaderContext(), root, parent)
        this.delegate = delegate

        if (delegate.isDefaultNamespace(root)) {
            val profileSpec = root.getAttribute(PROFILE_ATTRIBUTE)
            if (StringUtils.hasText(profileSpec)) {
                // TODO
            }
        }

        // 1.回调解析XML之前的钩子方法
        preProcessXml(root)

        // 2.真正地去解析XML
        parseBeanDefinitions(root, delegate)

        // 3.回调解析XML之后的钩子方法
        postProcessXml(root)

        // 在解析完成之后, 之前的delegate去恢复
        this.delegate = parent
    }

    /**
     * 创建解析BeanDefinition的Delegate
     *
     * @param readerContext ReaderContext
     * @param root root of Document
     * @param parent parent BeanDefinitionParserDelegate
     * @return 创建好得到的BeanDefinitionParserDelegate
     */
    protected open fun createDelegate(
        readerContext: XmlReaderContext,
        root: Element,
        parent: BeanDefinitionParserDelegate?
    ): BeanDefinitionParserDelegate {
        val delegate = BeanDefinitionParserDelegate(readerContext)
        delegate.init(root, parent)
        return delegate
    }

    /**
     * 从Document的Root元素的内部去解析BeanDefinitions
     *
     * @param root XML文件的Document的根元素
     * @param delegate 解析XML使用的delegate
     */
    protected open fun parseBeanDefinitions(root: Element, delegate: BeanDefinitionParserDelegate) {
        // 如果当前root标签是"beans"(或者空)的Namespace, 那么需要遍历它内部所有的元素去进行处理
        if (delegate.isDefaultNamespace(root)) {
            val childNodes = root.childNodes
            for (i in 0..childNodes.length) {
                val node = childNodes.item(i)
                if (node is Element) {
                    // 如果是默认的Namespace, 那么解析"import"/"bean"/"beans"等标签
                    if (delegate.isDefaultNamespace(node)) {
                        parseDefaultElement(node, delegate)

                        // 如果不是默认的Namespace, 那么需要根据NamespaceHandler去进行处理
                    } else {
                        delegate.parseCustomElement(node)
                    }
                }
            }

            // 如果root标签不是"beans", 也不是空的Namespace, 那么交给delegate去进行自定义标签的解析
        } else {
            delegate.parseCustomElement(root)
        }
    }

    /**
     * 解析Spring内部的默认Element, 包括"beans"内部的"import"、"bean"、"beans"等这些标签
     *
     * @param element element
     * @param delegate delegate
     */
    private fun parseDefaultElement(element: Element, delegate: BeanDefinitionParserDelegate) {
        // 如果当前是一个"import"标签, 那么说明需要导入一个XML配置文件
        if (delegate.nodeNameEquals(element, IMPORT_ELEMENT)) {
            importBeanDefinitionResource(element)
            // 如果当前是一个"bean"标签, 那么需要去解析成为BeanDefinition
        } else if (delegate.nodeNameEquals(element, BEAN_ELEMENT)) {
            processBeanDefinition(element, delegate)
            // 如果是一个嵌套的"beans"标签("beans"标签内部嵌套"beans"标签), 那么递归处理
        } else if (delegate.nodeNameEquals(element, NESTED_BEANS_ELEMENT)) {
            doRegisterBeanDefinitions(element)
        }
    }

    /**
     * 处理一个"bean"标签, 将它去解析成为BeanDefinition并注册到BeanDefinitionRegistry当中
     *
     * @param element 当前"bean"标签
     * @param delegate delegate
     */
    protected open fun processBeanDefinition(element: Element, delegate: BeanDefinitionParserDelegate) {
        // 将一个"bean"标签去解析成为一个BeanDefinition
        var bdHolder = delegate.parseBeanDefinitionElement(element)
        if (bdHolder != null) {

            // 如果必要的话, 对BeanDefinition去进行包装
            bdHolder = delegate.decorateBeanDefinitionIfRequired(element, bdHolder)

            // 将BeanDefinition注册到BeanDefinitionRegistry当中
            BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, delegate.readerContext.getRegistry())
        }
    }

    /**
     * 处理"import"标签, 导入Spring的XML文件
     *
     * @param element "import"标签的element
     */
    protected open fun importBeanDefinitionResource(element: Element) {
        var location = element.getAttribute(RESOURCE_ATTRIBUTE)
        if (!StringUtils.hasText(location)) {
            return
        }
        // 解析占位符
        location = getReaderContext().getEnvironment().resolvePlaceholders(location)
            ?: throw IllegalStateException("无法解析占位符[$location]")

        // 真正的资源
        val actualResources = LinkedHashSet<Resource>()

        // 判断该位置是一个绝对路径还是一个相对路径?
        var absoluteLocation = false
        try {
            absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute
        } catch (ex: URISyntaxException) {
            // ignore
        }
        if (absoluteLocation) {
            val count = getReaderContext().xmlBeanDefinitionReader.loadBeanDefinitions(location, actualResources)
            if (logger.isTraceEnabled) {
                logger.trace("从location=[$location]加载到[$count]个BeanDefinition")
            }
        } else {
            // TODO
        }
    }

    /**
     * 在进行XML的解析之前, 需要去进行的前置处理工作
     *
     * @param root Document根元素
     */
    protected open fun preProcessXml(root: Element) {}

    /**
     * 在进行XML的解析之后, 需要去进行的后置处理工作
     *
     * @param root Document根元素
     */
    protected open fun postProcessXml(root: Element) {}
}