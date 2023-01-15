package com.wanna.framework.beans

import com.wanna.framework.beans.PropertyAccessor.Companion.NESTED_PROPERTY_SEPARATOR_CHAR
import com.wanna.framework.beans.PropertyAccessor.Companion.PROPERTY_KEY_PREFIX
import com.wanna.framework.beans.PropertyAccessor.Companion.PROPERTY_KEY_PREFIX_CHAR
import com.wanna.framework.beans.PropertyAccessor.Companion.PROPERTY_KEY_SUFFIX
import com.wanna.framework.beans.PropertyAccessor.Companion.PROPERTY_KEY_SUFFIX_CHAR
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.util.StringUtils
import org.slf4j.LoggerFactory
import java.util.Optional
import kotlin.jvm.Throws


/**
 * 它为ConfigurablePropertyAccessor提供了典型的模板方法实现；
 *
 * 它也提供了基于ConversionService和PropertyEditor去进行类型转换的功能
 *
 * @see PropertyAccessor
 */
abstract class AbstractNestablePropertyAccessor() : AbstractPropertyAccessor() {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(AbstractNestablePropertyAccessor::class.java)
    }


    /**
     * 包装的对象
     */
    @Nullable
    protected var wrappedObject: Any? = null

    /**
     * root的对象, 因为存在有对象的嵌套, 因此就可能存在有root对象
     */
    @Nullable
    protected var rootObject: Any? = null

    /**
     * 当前[PropertyAccessor]所在的嵌套的路径
     */
    protected var nestedPath = ""

    /**
     * 维护当前的[PropertyAccessor]对应的嵌套的[PropertyAccessor]列表, Key-nestedPath, Value是该nestedPath的[PropertyAccessor]
     *
     * 其实nestedPath, 对应的就是一级的字段名, 当然对于Map/List等字段, 还可能有"address[0]"这样的格式
     */
    @Nullable
    protected var nestedPropertyAccessors: MutableMap<String, AbstractNestablePropertyAccessor>? = null

    /**
     * 获取包装的对象
     *
     * @return 包装的对象实例
     */
    open fun getWrappedInstance(): Any = this.wrappedObject ?: IllegalStateException("Target object must not be null")

    /**
     * 设置包装的对象
     *
     * @param obj 要去进行包装的对象
     */
    open fun setWrappedInstance(obj: Any) {
        this.wrappedObject = obj
    }

    /**
     * 获取包装的对象的类型
     *
     * @return 包装的对象的类型
     */
    open fun getWrappedClass(): Class<*> = getWrappedInstance()::class.java

    /**
     * 获取rootObject
     *
     * @return rootObject
     */
    open fun getRootInstance(): Any = this.rootObject ?: throw IllegalStateException("rootObject is null")

    /**
     * 获取rootObject的类型
     *
     * @return rootObject Class
     */
    open fun getRootClass(): Class<*> = getRootInstance().javaClass

    /**
     * 检查给定的属性名对应的属性是否是一个可读的属性
     * Note: 当属性本身就不存在的话, return false
     *
     * @param name 属性名(可能是一个嵌套的属性, 或者是一个indexed/mapped属性, 也就是支持使用'[]'去访问List/Map)
     * @return 如果该属性可读; 不存在的话, return false
     */
    override fun isReadableProperty(name: String): Boolean {
        try {
            val ph = getLocalPropertyHandler(name)
            if (ph != null) {
                return ph.readable
            } else {
                // Maybe an indexed/mapped property...
                getPropertyValue(name)
                return true
            }
        } catch (ex: InvalidPropertyException) {
            // Cannot be evaluated, so can't be readable.
        }
        return false
    }

    /**
     * 检查给定的属性名对应的属性是否是一个可写的属性?
     * Note: 当属性本身就不存在的话, return false
     *
     * @param name 属性名(可能是一个嵌套的属性, 或者是一个indexed/mapped属性, 也就是支持使用'[]'去访问List/Map)
     * @return 如果该属性值可写, return true; 不存在的话, return false
     */
    override fun isWritableProperty(name: String): Boolean {
        try {
            val ph = getLocalPropertyHandler(name)
            if (ph != null) {
                return ph.writeable
            } else {
                // Maybe an indexed/mapped property...
                getPropertyValue(name)
                return true
            }
        } catch (ex: InvalidPropertyException) {
            // Cannot be evaluated, so can't be readable.
        }
        return false
    }

    /**
     * 为给定的propertyPath去获取到对应的[PropertyAccessor], 提供属性的访问
     *
     * @param propertyPath propertyPath
     * @return 提供对于给定的propertyPath的属性的访问的[PropertyAccessor]
     */
    protected open fun getPropertyAccessorForPropertyPath(propertyPath: String): AbstractNestablePropertyAccessor {
        // 获取到propertyPath当中的"."的位置
        val pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath)

        // 如果存在有".", 那么说明是嵌套属性的情况
        if (pos != -1) {
            // 例如"user.address"的情况, 那么nestedProperty="user", nestedPath="address"
            val nestedProperty = propertyPath.substring(0, pos)
            val nestedPath = propertyPath.substring(pos + 1)

            // 根据nestedProperty去获取到对应的PropertyAccessor
            val nestedPropertyAccessor = getNestedPropertyAccessor(nestedProperty)

            // 对于nestedPath, 采用递归的方式去进行生成...
            return nestedPropertyAccessor.getPropertyAccessorForPropertyPath(nestedPath)

            // 如果不存在有"."的话, 那么直接return this
        } else {
            return this
        }
    }

    @Nullable
    protected open fun getPropertyValue(tokens: PropertyTokenHolder): Any? {
        return null
    }

    /**
     * 为给定的实例对象和nestedPath, 去创建[AbstractNestablePropertyAccessor], 模板方法, 交给子类去进行实现
     *
     * @param instance instance
     * @param nestedPath 嵌套的路径
     * @return 根据nestedPath创建的[AbstractNestablePropertyAccessor]
     */
    protected abstract fun newNestedPropertyAccessor(
        instance: Any,
        nestedPath: String
    ): AbstractNestablePropertyAccessor

    /**
     * 根据给定的[propertyName], 为它去获取一个[PropertyHandler], 去提供对于该属性的处理工作
     *
     * @param propertyName propertyName
     * @return 处理该属性的[PropertyHandler]
     */
    @Nullable
    protected abstract fun getLocalPropertyHandler(propertyName: String): PropertyHandler?

    /**
     * 为嵌套的属性, 去获取到[AbstractNestablePropertyAccessor]
     *
     * @param nestedProperty 嵌套的属性
     * @return 提供对于嵌套的属性的访问的[PropertyAccessor]
     */
    private fun getNestedPropertyAccessor(nestedProperty: String): AbstractNestablePropertyAccessor {
        if (this.nestedPropertyAccessors == null) {
            this.nestedPropertyAccessors = LinkedHashMap()
        }

        // 为nestedProperty去生成PropertyTokenHolder, 去对数组index, Map的Key的方式去进行描述
        val tokens = getPropertyNameTokens(nestedProperty)

        // 获取到canonicalName
        val canonicalName = tokens.canonicalName
        var value = getPropertyValue(tokens)

        // 如果value为空, 需要设置默认值/丢出异常...
        if (value == null || (value is Optional<*> && !value.isPresent)) {
            if (autoGrowNestedPaths) {
                value = setDefaultValue(tokens)
            } else {
                throw NullValueInNestedPathException(getRootClass(), this.nestedPath + canonicalName)
            }
        }

        var nestedPropertyAccessor = this.nestedPropertyAccessors!![canonicalName]

        // 如果之前缓存当中没有该属性的话, 那么创建一个新的PropertyAccessor并加入到缓存当中去
        if (nestedPropertyAccessor == null) {
            // nestedPath, 需要使用当前的nestedPath+canonicalName+"."
            nestedPropertyAccessor =
                newNestedPropertyAccessor(value, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR_CHAR)
            this.nestedPropertyAccessors!![canonicalName] = nestedPropertyAccessor
        } else {
            if (logger.isTraceEnabled) {
                logger.trace("Using cached nested property accessor for property '$canonicalName'")
            }
        }
        return nestedPropertyAccessor
    }

    private fun setDefaultValue(tokens: PropertyTokenHolder): Any {
        return Any()
    }

    /**
     * 从给定的propertyName当中获取到KeyEnd的位置
     *
     * @param propertyName 原始的propertyName
     * @param startIndex propertyName的keyStart的位置
     * @return keyEnd的位置index(不存在KeyEnd, return -1)
     */
    private fun getPropertyNameKeyEnd(propertyName: String, startIndex: Int): Int {
        // 记录中间的"["的重数, 对于"address[[name]id]"这种情况, 对于"[name]"这部分将会被跳过...
        var unclosedPrefixes = 0
        val length = propertyName.length
        for (i in startIndex until length) {
            if (propertyName[i] == PROPERTY_KEY_PREFIX_CHAR) {
                unclosedPrefixes++
            } else if (propertyName[i] == PROPERTY_KEY_SUFFIX_CHAR) {
                if (unclosedPrefixes == 0) {
                    return i
                } else {
                    unclosedPrefixes--
                }
            }
        }
        return -1
    }

    /**
     * 为给定的propertyName去转换成为[PropertyTokenHolder]
     *
     * @param propertyName 待转换的propertyName
     * @return 转换之后得到的PropertyTokenHolder
     */
    private fun getPropertyNameTokens(propertyName: String): PropertyTokenHolder {
        var actualName: String? = null
        val keys = ArrayList<String>()
        var searchIndex = 0
        while (searchIndex != -1) {
            val keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex)
            searchIndex = -1
            // 如果存在有"[1][2]"这种情况, 那么需要将1和2都收集到Key列表当中来
            if (keyStart != -1) {
                // keyStart为"["的位置, keyEnd为"]"的位置, 如果存在有"[]"表达式的话,
                // 那么actualName需要去掉这部分, "[]"这部分只是数组索引/Map的Key
                val keyEnd = getPropertyNameKeyEnd(propertyName, keyStart + PROPERTY_KEY_PREFIX.length)
                if (keyEnd != -1) {

                    // 真正的actualName只去计算第一次遇到"["之前的这部分
                    actualName = actualName ?: propertyName.substring(0, keyEnd)

                    // 切取"["和"]"之间的这部分作为Key
                    var key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length, keyEnd)

                    // 如果key带有引号, 那么把多余的引号去掉...(spring可真的太用心了)
                    if (key.length > 1
                        && (key.startsWith("'") && key.endsWith("'")
                                || key.startsWith("\"") && key.endsWith("\""))
                    ) {
                        key = key.substring(1, key.length - 1)
                    }
                    keys.add(key)

                    // searchIndex前进, 尝试去搜索后续的"[]"表达式
                    searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length
                }
            }
        }
        val token = PropertyTokenHolder(actualName ?: propertyName)
        if (keys.isNotEmpty()) {
            // 生成canonicalName, 生成格式为"address[0][1]"这样的格式
            token.canonicalName += PROPERTY_KEY_PREFIX +
                    StringUtils.collectionToCommaDelimitedString(keys, PROPERTY_KEY_PREFIX + PROPERTY_KEY_SUFFIX) +
                    PROPERTY_KEY_SUFFIX
            token.keys = keys.toTypedArray()
        }
        return token
    }

    /**
     * 设置属性值，应该使用setter的方式去进行设置
     *
     * @param name name
     * @param value value
     */
    override fun setPropertyValue(name: String, value: Any?) {
        val nestedPa = getPropertyAccessorForPropertyPath(name)
        val tokens = getPropertyNameTokens(getFinalPath(nestedPa, name))

        // 获取到Nested PropertyAccessor, 直接去setPropertyValue
        nestedPa.setPropertyValue(tokens, PropertyValue(name, value))
    }

    override fun setPropertyValue(propertyValue: PropertyValue) {
        var resolvedTokens = propertyValue.resolvedTokens as PropertyTokenHolder?

        // 如果之前还没解析过Token, 那么先去解析Token
        if (resolvedTokens == null) {
            val propertyName = propertyValue.name
            val nestPa = getPropertyAccessorForPropertyPath(propertyName)

            // 切取nestedPath的最后一段作为finalPath, 并解析成为Token
            resolvedTokens = getPropertyNameTokens(getFinalPath(nestPa, propertyName))

            // 解析完成Token之后, 去执行PropertyValue的设置
            nestPa.setPropertyValue(tokens = resolvedTokens, pv = propertyValue)

            // 如果之前已经解析过Token了...那么直接去进行PropertyValue的设置
        } else {
            this.setPropertyValue(resolvedTokens, propertyValue)
        }
    }

    /**
     * 执行属性值的设置
     *
     * @param tokens token
     * @param pv 要去进行设置的PropertyValue信息
     */
    protected open fun setPropertyValue(tokens: PropertyTokenHolder, pv: PropertyValue) {
        // 如果有Keys, 那么说明要设置到Map/List的内部元素当中
        if (tokens.keys != null) {
            processKeyedProperty(tokens, pv)

            // 如果没有Keys, 那么只需要去进行字段的设置
        } else {
            processLocalProperty(tokens, pv)
        }
    }

    private fun processKeyedProperty(tokens: PropertyTokenHolder, pv: PropertyValue) {
        // TODO
        val name = pv.name
        val value = pv.value
        // add: 采用setter的方式去设置属性值，替换之前的字段设置
        val writeMethodName = "set" + name[0].uppercase() + name.substring(1)
        var isFound = false
        ReflectionUtils.doWithMethods(getWrappedClass()) {
            if (isFound) {
                return@doWithMethods
            }
            val parameterTypes = it.parameterTypes
            if (it.name == writeMethodName && it.parameterCount == 1) {
                ReflectionUtils.makeAccessible(it)
                var targetToInject: Any? = value
                if (value is Collection<*> && !ClassUtils.isAssignFrom(Collection::class.java, parameterTypes[0])) {
                    targetToInject = if (value.isNotEmpty()) value.iterator().next() else null
                }
                val converted = convertIfNecessary(targetToInject, parameterTypes[0])
                ReflectionUtils.invokeMethod(it, getWrappedInstance(), converted)
                isFound = true
            }
        }
    }

    /**
     * 处理本地的属性值的设置
     *
     * @param tokens token
     * @param pv 要去进行设置的属性值PropertyValue
     */
    private fun processLocalProperty(tokens: PropertyTokenHolder, pv: PropertyValue) {
        val ph = getLocalPropertyHandler(tokens.actualName)
        if (ph != null && ph.writeable) {
            val originValue = pv.value
            var valueToApply = originValue
            var oldValue: Any? = null

            // 如果这个PropertyValue之前已经完成了转换, 那么直接获取转换之后的值即可...
            if (pv.isConverted()) {
                valueToApply = pv.getConvertedValue()

                // 如果这个PropertyValue之前还没完成转换, 那么在这里去进行转换
            } else {
                // 检查是否需要把oldValue提供给PropertyEditor?
                if (extractOldValueForEditor && ph.readable) {
                    oldValue = ph.getValue()
                }

                // convert for property...
                valueToApply = convertForProperty(tokens.actualName, oldValue, valueToApply, ph.toTypeDescriptor())
            }

            // 获取到完成类型转换之后的值之后, 利用PropertyHandler, 去进行值的设置...
            ph.setValue(valueToApply)
        } else {
            // TODO
        }
    }

    /**
     * 根据nestedPath, 去获取到它的最后一段路径, 例如"user.address[0]", 此时需要返回"address[0]"
     *
     * @param pa PropertyAccessor
     * @param nestedPath nestedPath
     */
    protected open fun getFinalPath(pa: AbstractNestablePropertyAccessor, nestedPath: String): String {
        if (pa == this) {
            return nestedPath
        }
        return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath))
    }

    /**
     * 针对给定的属性, 去完成类型转换
     *
     * @param propertyName 属性名
     * @param oldValue 属性旧值
     * @param newValue 属性新值
     * @return 经过属性转换之后的属性值
     */
    @Nullable
    @Throws(TypeMismatchException::class)
    protected open fun convertForProperty(
        propertyName: String,
        @Nullable oldValue: Any?,
        @Nullable newValue: Any?,
        td: TypeDescriptor
    ): Any? {
        return convertIfNecessary(propertyName, oldValue, newValue, td)
    }

    @Nullable
    private fun convertIfNecessary(
        propertyName: String,
        @Nullable oldValue: Any?,
        @Nullable newValue: Any?,
        td: TypeDescriptor
    ): Any? {
        var valueToApply: Any? = newValue
        if (valueToApply is Collection<*> &&
            !ClassUtils.isAssignFrom(Collection::class.java, td.type)
        ) {
            valueToApply = if (valueToApply.isNotEmpty()) valueToApply.iterator().next() else null
        }

        val delegate = this.delegate ?: throw IllegalStateException("No TypeConverterDelegate")

        // 使用TypeConverterDelegate, 去完成类型转换
        valueToApply = delegate.convertIfNecessary(propertyName, oldValue, valueToApply!!, td.type)
        return valueToApply
    }

    override fun getPropertyValue(name: String): Any? {
        // add: 采用getter的方式去获取属性值
        val readMethodName = "get" + name[0].uppercase() + name.substring(1)
        var isFound = false
        var returnValue: Any? = null
        ReflectionUtils.doWithMethods(getWrappedClass()) {
            if (isFound) {
                return@doWithMethods
            }
            if (it.name == readMethodName && it.parameterCount == 0) {
                ReflectionUtils.makeAccessible(it)
                returnValue = ReflectionUtils.invokeMethod(it, getWrappedInstance())
                isFound = true
            }
        }
        return returnValue
    }

    /**
     * 对于一个特定的属性去进行处理的Handler
     */
    protected abstract class PropertyHandler(
        val propertyType: Class<*>,
        val readable: Boolean,
        val writeable: Boolean
    ) {

        /**
         * 将这个属性的类型, 去转换成为[ResolvableType]
         *
         * @return ResolvableType for propertyType
         */
        abstract fun getResolvableType(): ResolvableType

        /**
         * 将这个属性值的类型, 转换成为[TypeDescriptor]
         *
         * @return TypeHandler for propertyType
         */
        abstract fun toTypeDescriptor(): TypeDescriptor

        /**
         * 获取该属性的值
         *
         * @return propertyValue
         */
        @Nullable
        @Throws(Exception::class)
        abstract fun getValue(): Any?

        /**
         * 对该属性的值去进行设置
         *
         * @param value value to set
         */
        @Throws(Exception::class)
        abstract fun setValue(@Nullable value: Any?)
    }

    /**
     * PropertyToken, 对于属性Key去进行描述
     * 例如对于给定的是"address"这样的正常的表达式, actualName="address", canonicalName="address", keys=null;
     * 对于给定的是"address[1][2]"这样的表达式, 那么actualName="address", canonicalName="address[1][2]", keys=[1,2]
     *
     * @param actualName 真正的属性名
     * @param canonicalName 规范的属性名
     * @param keys keys(Map的Key, List的索引)
     */
    class PropertyTokenHolder(var actualName: String, var canonicalName: String, @Nullable var keys: Array<String>?) {
        constructor(name: String) : this(name, name, null)
    }
}