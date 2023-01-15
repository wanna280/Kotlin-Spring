package com.wanna.framework.beans.factory.xml

import com.wanna.framework.beans.PropertyValue
import com.wanna.framework.beans.factory.config.RuntimeBeanReference
import com.wanna.framework.beans.factory.config.TypedStringValue
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.BeanDefinitionReaderUtils.createBeanDefinition
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttribute
import com.wanna.framework.beans.method.LookupOverride
import com.wanna.framework.beans.method.ReplaceOverride
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.StringUtils
import com.wanna.framework.util.StringUtils.commaDelimitedListToStringArray
import org.slf4j.LoggerFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.*

/**
 * BeanDefinition的解析器的Delegate, 提供真正地去解析和注册BeanDefinition的功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 *
 * @see NamespaceHandler
 * @see NamespaceHandlerResolver
 *
 * @param readerContext XmlReaderContext
 */
class BeanDefinitionParserDelegate(val readerContext: XmlReaderContext) {

    companion object {
        /**
         * Beans标签的NamespaceUri
         */
        const val BEANS_NAMESPACE_URI = "http://www.springframework.org/schema/beans"

        /**
         * true的字符串常量
         */
        const val TRUE_VALUE = "true"

        /**
         * false的字符串常量
         */
        const val FALSE_VALUE = "false"

        /**
         * default常量
         */
        const val DEFAULT_VALUE = "default"

        /**
         * property标签
         */
        const val PROPERTY_ELEMENT = "property"

        /**
         * lookup-method标签
         */
        const val LOOKUP_METHOD_ELEMENT = "lookup-method"

        /**
         * meta标签
         */
        const val META_ELEMENT = "meta"

        /**
         * description属性
         */
        const val DESCRIPTION_ATTRIBUTE = "description"

        /**
         * replaced-method标签
         */
        const val REPLACED_METHOD_ELEMENT = "replaced-method"

        /**
         * scope属性
         */
        const val SCOPE_ATTRIBUTE = "scope"

        /**
         * abstract属性
         */
        const val ABSTRACT_ATTRIBUTE = "abstract"

        /**
         * lazy-init
         */
        const val LAZY_INIT_ATTRIBUTE = "lazy-init"

        /**
         * autowire-candidate属性
         */
        const val AUTOWIRE_CANDIDATE_ATTRIBUTE = "autowire-candidate"

        /**
         * autowire-mode属性
         */
        const val AUTOWIRE_MODE_ATTRIBUTE = "autowire-mode"

        /**
         * 不进行自动注入
         */
        const val AUTOWIRE_NO_VALUE = "no"

        /**
         * byName注入
         */
        const val AUTOWIRE_BY_NAME = "byName"

        /**
         * byType注入
         */
        const val AUTOWIRE_BY_TYPE = "byType"

        /**
         * 构造器注入
         */
        const val AUTOWIRE_CONSTRACUTOR_VALUE = "constructor"

        /**
         * depends-on属性
         */
        const val DEPENDS_ON_ATTRIBUTE = "depends-on"

        /**
         * primary属性
         */
        const val PRIMARY_ATTRIBUTE = "primary"

        /**
         * init-method属性
         */
        const val INIT_METHOD_ATTRIBUTE = "init-method"

        /**
         * factory-method属性
         */
        const val FACTORY_METHOD_ATTRIBUTE = "factory-method"

        /**
         * factory-bean属性
         */
        const val FACTORY_BEAN_ATTRIBUTE = "factory-bean"

        /**
         * destroy-method属性
         */
        const val DESTROY_METHOD_ATTRIBUTE = "destroy-method"

        /**
         * replacer属性
         */
        const val REPLACER_ATTRIBUTE = "replacer"

        /**
         * Bean标签
         */
        const val BEAN_ELEMENT = "bean"

        /**
         * key属性
         */
        const val KEY_ATTRIBUTE = "key"

        /**
         * value属性
         */
        const val VALUE_ATTRIBUTE = "value"

        /**
         * ref属性
         */
        const val REF_ATTRIBUTE = "ref"

        /**
         * id属性
         */
        const val ID_ATTRIBUTE = "id"

        /**
         * name属性
         */
        const val NAME_ATTRIBUTE = "name"

        /**
         * class属性(指定beanClass)
         */
        const val CLASS_ATTRIBUTE = "class"

        /**
         * parent属性
         */
        const val PARENT_ATTRIBUTE = "parent"

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(BeanDefinitionParserDelegate::class.java)
    }


    fun init(root: Element, @Nullable parent: BeanDefinitionParserDelegate?) {

    }

    /**
     * 解析自定义XML元素(需要使用到NamespaceHandler去提供BeanDefinition的解析)
     *
     * @param element element
     * @return 解析到的BeanDefinition(如果解析失败, 那么return null)
     */
    @Nullable
    fun parseCustomElement(element: Element): BeanDefinition? = parseCustomElement(element, null)

    /**
     * 解析自定义XML元素(需要使用到NamespaceHandler去提供BeanDefinition的解析)
     *
     * @param element element
     * @param containingBd 已经存在有的BeanDefinition(如果有的话)
     * @return 解析到的BeanDefinition(如果解析失败, 那么return null)
     */
    @Nullable
    fun parseCustomElement(element: Element, @Nullable containingBd: BeanDefinition?): BeanDefinition? {
        val namespaceUri = getNamespaceUri(element) ?: return null
        val namespaceHandler = readerContext.xmlBeanDefinitionReader.getNamespaceHandlerResolver().resolve(namespaceUri)
        if (namespaceHandler != null) {
            return namespaceHandler.parse(element, ParserContext(readerContext, this, containingBd))
        }
        logger.error("无法为[$namespaceUri]去找到合适的NamespaceHandler去进行解析")
        return null
    }

    /**
     * 解析一个"bean"标签成为BeanDefinition
     *
     * @param element element
     * @return 解析到的BeanDefinitionHolder
     */
    @Nullable
    fun parseBeanDefinitionElement(element: Element): BeanDefinitionHolder? = parseBeanDefinitionElement(element, null)

    /**
     * 解析一个"bean"标签成为BeanDefinition
     *
     * @param element element
     * @param containingBd 已经存在有的BeanDefinition(如果有的话)
     * @return 解析到的BeanDefinitionHolder
     */
    @Nullable
    fun parseBeanDefinitionElement(element: Element, @Nullable containingBd: BeanDefinition?): BeanDefinitionHolder? {
        val id = element.getAttribute(ID_ATTRIBUTE)
        val name = element.getAttribute(NAME_ATTRIBUTE)
        var beanName = id
        if (!StringUtils.hasText(beanName)) {
            beanName = name
        }

        // 解析BeanDefinition, 并填充属性信息
        val beanDefinition = parseBeanDefinitionElement(element, beanName, containingBd)
        if (beanDefinition != null) {

            // 如果还是不存在有beanName, 那么我们去生成beanName
            if (!StringUtils.hasText(beanName)) {
                beanName = readerContext.generateBeanName(beanDefinition)
            }
            return BeanDefinitionHolder(beanDefinition, beanName)
        }
        return null
    }

    /**
     * 将一个"bean"标签去解析成为一个BeanDefinition, 比如"class"属性, 比如"parent"属性
     *
     * @param element 一个"bean"标签的Element
     * @param beanName 从"Element"当中去解析到的beanName
     * @param containingBd 已经包含的BeanDefinition(如果存在的话)
     * @return 从一个"bean"标签的Element当中去解析到的BeanDefinition, 解析失败return null
     */
    @Nullable
    fun parseBeanDefinitionElement(
        element: Element,
        beanName: String,
        containingBd: BeanDefinition?
    ): AbstractBeanDefinition? {

        // 1.解析beanClassName
        var beanClassName: String? = null
        if (element.hasAttribute(CLASS_ATTRIBUTE)) {
            beanClassName = element.getAttribute(CLASS_ATTRIBUTE).trim()
        }

        // 2.解析parentBeanDefinitionName
        var parent: String? = null
        if (element.hasAttribute(PARENT_ATTRIBUTE)) {
            parent = element.getAttribute(PARENT_ATTRIBUTE).trim()
        }

        // 根据beanClass去创建出来BeanDefinition
        val definition = createBeanDefinition(parent, beanClassName, readerContext.getBeanClassLoader())
        // 解析BeanDefinition当中的各个的属性
        parseBeanDefinitionAttributes(element, beanName, containingBd, definition)

        // 解析BeanDescription
        parseDescription(element, definition)

        // 解析"meta"标签, 将它解析成为BeanDefinition的Attribute
        parseMetaElements(element, definition)

        // 解析"lookup-method"这个标签成为MethodOverride加入到BeanDefinition
        parseLookupOverrideSubElements(element, definition)

        // 解析"replaced-method"这个标签成为ReplaceMethodOverride加入到BeanDefinition
        parseReplacedMethodSubElements(element, definition)

        // 解析"bean"标签内部配置的"property"标签
        parsePropertyElements(element, definition)

        // 设置Resource
        definition.setResource(this.readerContext.resource)
        return definition
    }

    /**
     * 解析BeanDefinition的描述信息
     *
     * @param element "bean"标签的Element
     * @param bd 正在解析的BeanDefinition
     */
    fun parseDescription(element: Element, bd: AbstractBeanDefinition) {
        val childNodes = element.childNodes
        for (i in 0..childNodes.length) {
            val node = childNodes.item(i)
            if (isCandidateElement(node) && nodeNameEquals(node, DESCRIPTION_ATTRIBUTE)) {
                bd.setDescription((node as Element).getAttribute(VALUE_ATTRIBUTE))
            }
        }
    }

    /**
     * 解析"meta"标签, 将它添加到BeanDefinition的属性(Attribute)当中
     *
     * @param element "bean"标签的Element
     * @param bd 正在解析的BeanDefinition
     */
    fun parseMetaElements(element: Element, bd: AbstractBeanDefinition) {
        val childNodes = element.childNodes
        for (i in 0..childNodes.length) {
            val node = childNodes.item(i)
            if (isCandidateElement(node) && nodeNameEquals(node, META_ELEMENT)) {
                node as Element
                val key = node.getAttribute(KEY_ATTRIBUTE)
                val value = node.getAttribute(VALUE_ATTRIBUTE)
                val attribute = BeanMetadataAttribute(key, value)
                bd.addAttribute(key, attribute)
            }
        }
    }

    /**
     * 解析"lookup-method"标签, 将它解析成为一个[LookupOverride]并添加到BeanDefinition当中
     *
     * @param element "bean"标签的Element
     * @param bd 正在解析的BeanDefinition
     */
    fun parseLookupOverrideSubElements(element: Element, bd: AbstractBeanDefinition) {
        val childNodes = element.childNodes
        for (i in 0..childNodes.length) {
            val node = childNodes.item(i)
            if (isCandidateElement(node) && nodeNameEquals(node, LOOKUP_METHOD_ELEMENT)) {
                node as Element
                val name = node.getAttribute(NAME_ATTRIBUTE)
                val bean = node.getAttribute(BEAN_ELEMENT)
                val lookupOverride = LookupOverride(name, bean)
                bd.getMethodOverrides().addMethodOverride(lookupOverride)
            }
        }
    }

    /**
     * 解析"replaced-method"标签, 将它解析成为一个[ReplaceOverride]并添加到BeanDefinition当中
     *
     * @param element element
     * @param bd 正在解析的BeanDefinition
     */
    fun parseReplacedMethodSubElements(element: Element, bd: AbstractBeanDefinition) {
        val childNodes = element.childNodes
        for (i in 0..childNodes.length) {
            val node = childNodes.item(i)
            if (isCandidateElement(node) && nodeNameEquals(node, REPLACED_METHOD_ELEMENT)) {
                node as Element
                val name = node.getAttribute(NAME_ATTRIBUTE)
                val replacer = node.getAttribute(REPLACER_ATTRIBUTE)
                val replaceOverride = ReplaceOverride(name, replacer)
                bd.getMethodOverrides().addMethodOverride(replaceOverride)
            }
        }
    }

    /**
     * 解析"bean"标签下面的所有的"property"标签(遍历所有的子标签去进行处理, 找到所有的"property"标签)
     *
     * @param element Element
     * @param bd 正在解析的BeanDefinition
     */
    fun parsePropertyElements(element: Element, bd: AbstractBeanDefinition) {
        val childNodes = element.childNodes
        for (i in 0..childNodes.length) {
            val node = childNodes.item(i)
            // 如果当前Node是一个"property"标签的话
            if (!Objects.isNull(node) && isCandidateElement(node) && nodeNameEquals(node, PROPERTY_ELEMENT)) {
                parsePropertyElement(node as Element, bd)
            }
        }
    }

    /**
     * 解析一个"property"标签, 并添加到BeanDefinition当中
     *
     * @param element "property"标签的Element
     * @param bd 正在解析的BeanDefinition
     */
    fun parsePropertyElement(element: Element, bd: AbstractBeanDefinition) {
        val name = element.getAttribute(NAME_ATTRIBUTE)
        if (!StringUtils.hasText(name)) {
            return
        }
        if (bd.getPropertyValues().containsProperty(name)) {
            return
        }
        val parsedPropertyValue = parsePropertyValue(element, bd, name)
        val propertyValue = PropertyValue(name, parsedPropertyValue)
        bd.getPropertyValues().addPropertyValue(propertyValue)
    }

    /**
     * 解析一个"property"标签当中配置的propertyValue, 可能是"ref"字段, 也可能是"value"字段
     *
     * @param element "property"标签
     * @param bd BeanDefinition
     * @param propertyName propertyName
     * @return RuntimeBeanReference或者TypedStringValue(当然也可以为null)
     */
    @Nullable
    fun parsePropertyValue(element: Element, bd: AbstractBeanDefinition, @Nullable propertyName: String?): Any? {
        val ref = element.getAttribute(REF_ATTRIBUTE)
        val value = element.getAttribute(VALUE_ATTRIBUTE)

        // 如果是一个ref, 那么创建一个RuntimeBeanReference
        if (StringUtils.hasText(ref)) {
            return RuntimeBeanReference(ref)

            // 如果是一个value, 那么创建TypedStringValue
        } else if (StringUtils.hasText(value)) {
            return TypedStringValue(value)
        }
        return null
    }

    /**
     * 判断Node是否一个候选的Element？只有它是一个默认的namespace, 或者它的parentNode不是一个默认的namespace才行
     *
     * @param node Node
     * @return 如果是一个候选Element, 那么return true; 否则return false
     */
    private fun isCandidateElement(node: Node?): Boolean =
        node is Element && (isDefaultNamespace(node) || !isDefaultNamespace(node.parentNode))

    /**
     * 如果必要的话, 需要去解析BeanDefinition的一些属性信息,
     * 包括"scope"/"lazyInit"/"autowireMode"/"autowireCandidate"/"initMethod"/"destroyMethod"等一系列的属性
     *
     * @param element "bean"标签的Element
     * @param beanName beanName
     * @param containingBd 已经存在有的BeanDefinition(如果有的话)
     * @param bd BeanDefinition
     */
    fun parseBeanDefinitionAttributes(
        element: Element,
        beanName: String,
        @Nullable containingBd: BeanDefinition?,
        bd: AbstractBeanDefinition
    ) {
        // 设置scope属性
        if (element.hasAttribute(SCOPE_ATTRIBUTE)) {
            bd.setScope(element.getAttribute(SCOPE_ATTRIBUTE))
        } else if (containingBd != null) {
            bd.setScope(containingBd.getScope())
        }

        // 设置abstract属性(默认为false, 只有配置了true才会为true)
        if (element.hasAttribute(ABSTRACT_ATTRIBUTE)) {
            bd.setAbstract(element.getAttribute(ABSTRACT_ATTRIBUTE) == TRUE_VALUE)
        }

        // 设置lazyInit属性(默认为false, 只有配置了true才会为true)
        val lazyInit = element.getAttribute(LAZY_INIT_ATTRIBUTE)
        if (!StringUtils.hasText(lazyInit)) {
            bd.setLazyInit(TRUE_VALUE == lazyInit)
        }

        // 设置autowireCandidate属性(默认为true, 如果配置了false则为false)
        if (element.hasAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE)) {
            bd.setAutowireCandidate(FALSE_VALUE != element.getAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE))
        }

        // 设置autowireMode属性(byName/byType/constructor/no)
        if (element.hasAttribute(AUTOWIRE_MODE_ATTRIBUTE)) {
            bd.setAutowireMode(getAutowireMode(element.getAttribute(AUTOWIRE_MODE_ATTRIBUTE)))
        }

        // 设置dependsOn属性
        if (element.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
            val dependsOn = commaDelimitedListToStringArray(element.getAttribute(DEPENDS_ON_ATTRIBUTE))
            bd.setDependsOn(dependsOn)
        }

        // 设置primary属性(默认为false, 只有配置了true才会为true)
        if (element.hasAttribute(PRIMARY_ATTRIBUTE)) {
            bd.setPrimary(TRUE_VALUE == element.getAttribute(PRIMARY_ATTRIBUTE))
        }

        // 设置initMethod属性
        if (element.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
            bd.setInitMethodName(element.getAttribute(INIT_METHOD_ATTRIBUTE))
        }

        // 设置destroyMethod属性
        if (element.hasAttribute(DESTROY_METHOD_ATTRIBUTE)) {
            bd.setDestroyMethodName(element.getAttribute(DESTROY_METHOD_ATTRIBUTE))
        }

        // 设置factoryBeanName属性
        if (element.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
            bd.setFactoryBeanName(element.getAttribute(FACTORY_BEAN_ATTRIBUTE))
        }

        // 设置factoryMethod属性
        if (element.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
            bd.setFactoryMethodName(element.getAttribute(FACTORY_METHOD_ATTRIBUTE))
        }
    }

    /**
     * 根据字符串的autowireMode解析成为int的autowireMode
     *
     * @param mode modeStr
     * @return modeInt
     */
    private fun getAutowireMode(mode: String): Int {
        return when (mode) {
            AUTOWIRE_NO_VALUE -> AbstractBeanDefinition.AUTOWIRE_NO
            AUTOWIRE_BY_NAME -> AbstractBeanDefinition.AUTOWIRE_BY_NAME
            AUTOWIRE_BY_TYPE -> AbstractBeanDefinition.AUTOWIRE_BY_TYPE
            AUTOWIRE_CONSTRACUTOR_VALUE -> AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR
            else -> AbstractBeanDefinition.AUTOWIRE_NO
        }
    }

    /**
     * 如果必要的话, 需要去对BeanDefinition去进行包装
     *
     * @param element element
     * @param origin 原始的BeanDefinitionHolder
     * @return 包装之后的BeanDefinitionHolder
     */
    fun decorateBeanDefinitionIfRequired(element: Element, origin: BeanDefinitionHolder): BeanDefinitionHolder =
        decorateBeanDefinitionIfRequired(element, origin, null)


    /**
     * 如果必要的话, 需要去对BeanDefinition去进行包装
     *
     * @param element element
     * @param origin 原始的BeanDefinitionHolder
     * @param containingBd 已经存在有的BeanDefinition(如果有的话)
     * @return 包装之后的BeanDefinitionHolder
     */
    fun decorateBeanDefinitionIfRequired(
        element: Element,
        origin: BeanDefinitionHolder,
        @Nullable containingBd: BeanDefinition?
    ): BeanDefinitionHolder {
        // 包装的结果的BeanDefinitionHolder
        var finalDefinition = origin

        // 1.遍历所有的属性, 尝试去进行包装
        val attributes = element.attributes
        for (i in 0..attributes.length) {
            val node = attributes.item(i)
            finalDefinition = decorateIfRequired(node, finalDefinition, containingBd)
        }
        // 2.遍历所有的ChildNode, 尝试去进行包装
        val childNodes = element.childNodes
        for (i in 0..childNodes.length) {
            val node = childNodes.item(i)
            if (!Objects.isNull(node) && node.nodeType == Node.ELEMENT_NODE) {
                finalDefinition = decorateIfRequired(node, finalDefinition, containingBd)
            }
        }
        return finalDefinition
    }

    /**
     * 如果必要的话, 对原始的BeanDefinition去进行包装
     *
     * @param node Node
     * @param originDef 原始的BeanDefinition
     * @param containingBd 已经包含的BeanDefinition(如果有的话)
     */
    fun decorateIfRequired(
        node: Node?,
        originDef: BeanDefinitionHolder,
        @Nullable containingBd: BeanDefinition?
    ): BeanDefinitionHolder {
        node ?: return originDef
        val namespaceUri = getNamespaceUri(node)
        if (!Objects.isNull(namespaceUri) && !isDefaultNamespace(namespaceUri)) {
            val namespaceHandler = this.readerContext.namespaceHandlerResolver.resolve(namespaceUri!!)
            if (namespaceHandler != null) {
                val decorated =
                    namespaceHandler.decorate(node, originDef, ParserContext(readerContext, this, containingBd))

                // 如果包装成功, 那么返回包装之后的结果
                if (decorated != null) {
                    return decorated
                }
            }
        }
        return originDef
    }

    /**
     * 判断给定的namespaceUri是否是一个默认Namespace
     *
     * @param namespaceUri namespaceUri
     * @return 如果它是Spring默认的NamespaceUri(namespaceUri为空, 或者是namespaceUri为beans), 那么return true; 否则return false
     */
    fun isDefaultNamespace(@Nullable namespaceUri: String?): Boolean =
        !StringUtils.hasText(namespaceUri) || Objects.equals(BEANS_NAMESPACE_URI, namespaceUri)

    /**
     * 判断给定的Node是否是默认的Namespace？
     *
     * @param node node
     * @return 如果namespaceUri是Spring的默认Namespace, 那么return true; 否则return false
     */
    fun isDefaultNamespace(node: Node): Boolean = isDefaultNamespace(getNamespaceUri(node))

    /**
     * 从一个Node当中去获取到该Node的NamespaceUri
     *
     * @param node node
     * @return 解析到的NamespaceUri(如果不存在, 那么return null)
     */
    @Nullable
    fun getNamespaceUri(node: Node): String? = node.namespaceURI

    /**
     * 判断给定的node的name是否和目标desiredName匹配？
     *
     * @param node node
     * @param desiredName 待匹配的nodeName
     */
    fun nodeNameEquals(node: Node, desiredName: String): Boolean =
        Objects.equals(node.nodeName, desiredName) || Objects.equals(getLocalName(node), desiredName)

    /**
     * 获取Node的localName
     *
     * @param node node
     * @return nodeLocalName
     */
    fun getLocalName(node: Node): String? = node.localName
}