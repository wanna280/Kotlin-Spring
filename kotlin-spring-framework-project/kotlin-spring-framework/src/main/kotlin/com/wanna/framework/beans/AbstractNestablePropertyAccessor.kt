package com.wanna.framework.beans

import com.wanna.framework.beans.PropertyAccessor.Companion.NESTED_PROPERTY_SEPARATOR_CHAR
import com.wanna.framework.beans.PropertyAccessor.Companion.PROPERTY_KEY_PREFIX
import com.wanna.framework.beans.PropertyAccessor.Companion.PROPERTY_KEY_PREFIX_CHAR
import com.wanna.framework.beans.PropertyAccessor.Companion.PROPERTY_KEY_SUFFIX
import com.wanna.framework.beans.PropertyAccessor.Companion.PROPERTY_KEY_SUFFIX_CHAR
import com.wanna.framework.core.CollectionFactory
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import org.slf4j.LoggerFactory
import java.lang.StringBuilder
import java.lang.reflect.Modifier
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
     * 自动递增的限制
     */
    private var autoGrowCollectionLimit = Int.MAX_VALUE


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
        this.setWrappedInstance(obj, null, null)
    }

    /**
     * 设置包装的对象
     *
     * @param obj 要去进行包装的对象
     * @param nestedPath 嵌套的路径
     * @param rootObject rootObject
     */
    open fun setWrappedInstance(obj: Any, @Nullable nestedPath: String?, @Nullable rootObject: Any?) {
        this.wrappedObject = obj
        this.nestedPropertyAccessors = null
        this.nestedPath = nestedPath ?: ""
        this.rootObject = if (this.nestedPath.isNotEmpty()) rootObject else wrappedObject
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
     * 给定一个propertyName, 去获取到该Property对应的属性值类型
     *
     * @param name propertyName
     * @return 该属性值的类型(如果获取不到, return null)
     */
    @Nullable
    override fun getPropertyType(name: String): Class<*>? {
        try {
            // 1.检查本地的属性?
            val ph = getLocalPropertyHandler(name)
            if (ph != null) {
                return ph.propertyType
            }
            // 2.检查嵌套的属性?
            val value = getPropertyValue(name)
            if (value != null) {
                return value.javaClass
            }

        } catch (ex: Exception) {
            // Consider as not determinable.
        }
        return null
    }

    /**
     * 设置属性值，应该使用setter的方式去进行设置
     *
     * @param name name
     * @param value value
     */
    override fun setPropertyValue(name: String, @Nullable value: Any?) {
        val nestedPa = getPropertyAccessorForPropertyPath(name)
        val tokens = getPropertyNameTokens(getFinalPath(nestedPa, name))

        // 获取到Nested PropertyAccessor, 直接去setPropertyValue
        nestedPa.setPropertyValue(tokens, PropertyValue(name, value))
    }

    /**
     * 设置PropertyValue, 对指定的Property的值去进行设置
     *
     * @param propertyValue 要去进行设置的PropertyValue
     */
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
     * 根据name去获取到Property的Value
     *
     * @param name propertyName
     * @return 根据propertyName去获取到的Property的Value
     */
    @Nullable
    override fun getPropertyValue(name: String): Any? {
        val nestedPa = getPropertyAccessorForPropertyPath(name)
        val tokens = getPropertyNameTokens(getFinalPath(nestedPa, name))
        // 支持嵌套(indexed/mapped)的方式, 去为给定的属性名, 去进行属性值的解析
        return nestedPa.getPropertyValue(tokens)
    }

    /**
     * 为指定的属性名对应的属性的类型, 去获取到[TypeDescriptor]
     *
     * @param name 属性名(可能是一个嵌套的属性, 或者是一个indexed/mapped属性, 也就是支持使用'[]'去访问List/Map)
     * @return 获取到的对应的属性对应的TypeDescriptor(获取不到return null)
     */
    @Nullable
    override fun getPropertyTypeDescriptor(name: String): TypeDescriptor? {
        try {
            val nestedPa = getPropertyAccessorForPropertyPath(name)
            val tokens = getPropertyNameTokens(getFinalPath(nestedPa, name))
            val ph = nestedPa.getLocalPropertyHandler(tokens.actualName)
            if (ph != null) {
                if (tokens.keys != null) {
                    if (ph.readable || ph.writeable) {
                        return ph.nested(tokens.keys!!.size)
                    }
                } else {
                    if (ph.readable || ph.writeable) {
                        return ph.toTypeDescriptor()
                    }
                }
            }
        } catch (ex: InvalidPropertyException) {

        }
        return null
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
        return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath) + 1)
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

    /**
     * 为给定的tokens[PropertyTokenHolder]去获取到具体的属性值
     *
     * @param tokens 为属性名去完成解析之后得到的tokens
     * @return 根据给定的tokens去获取到的属性值
     */
    @Nullable
    protected open fun getPropertyValue(tokens: PropertyTokenHolder): Any? {
        val propertyName = tokens.canonicalName
        val actualName = tokens.actualName
        val ph = getLocalPropertyHandler(actualName)
        // 如果不存在有Getter, 那么...
        if (ph == null || !ph.readable) {
            throw NotReadablePropertyException(getRootClass(), this.nestedPath + propertyName)
        }
        var value: Any? = ph.getValue()
        if (tokens.keys != null) {
            if (value == null) {
                if (autoGrowNestedPaths) {
                    value = setDefaultValue(PropertyTokenHolder(propertyName))
                } else {
                    throw NullValueInNestedPathException(
                        getRootClass(),
                        this.nestedPath + propertyName,
                        "Cannot access indexed value of property referenced in indexed property path '$propertyName': returned null"
                    )
                }
            }
            val indexedPropertyName = StringBuilder(propertyName)
            for (i in 0 until tokens.keys!!.size) {
                val key = tokens.keys!![i]
                if (value == null) {
                    throw NullValueInNestedPathException(
                        getRootClass(),
                        this.nestedPath + propertyName,
                        "Cannot access indexed value of property referenced in indexed property path '$propertyName': returned null"
                    )
                }
                if (value.javaClass.isArray) {
                    val index = key.toInt()
                    growArrayIfNecessary(value, index, indexedPropertyName.toString())
                    value = java.lang.reflect.Array.get(value, index)
                } else if (value is List<*>) {
                    val index = key.toInt()
                    growCollectionIfNecessary(value, index, propertyName, ph, i + 1)
                    value = value[index]
                } else if (value is Set<*>) {
                    val index = key.toInt()
                    // 对于Set, 无法扩容...size不够直接异常
                    if (index < 0 || index >= value.size) {
                        throw InvalidPropertyException(
                            getRootClass(), this.nestedPath + propertyName,
                            "Cannot get element with index " + index + " from Set of size " +
                                    value.size + ", accessed using property path '" + propertyName + "'"
                        )
                    }
                    // 手动迭代K次, 去获取到Set当中的该位置的元素
                    val iterator = value.iterator()
                    for (k in 0..index) {
                        val next = iterator.next()
                        if (k == index) {
                            value = next
                            break
                        }
                    }
                } else if (value is Map<*, *>) {
                    // TODO, not supported nested, only support string
                    val elementType = String::class.java
                    // 对Key去进行convert
                    val convertedKey =
                        convertIfNecessary(null, null, key, elementType, TypeDescriptor.forClass(elementType))
                    value = value[convertedKey]
                } else {
                    throw InvalidPropertyException(
                        getRootClass(), this.nestedPath + propertyName,
                        "Property referenced in indexed property path '" + propertyName +
                                "' is neither an array nor a List nor a Set nor a Map; returned value was [" + value + "]"
                    )
                }
                // 把当前正在处理的Key, 去添加到indexedPropertyName当中...
                indexedPropertyName.append(PROPERTY_KEY_PREFIX).append(key).append(PROPERTY_KEY_SUFFIX)
            }
        }
        return value
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
        @Nullable propertyName: String?,
        @Nullable oldValue: Any?,
        @Nullable newValue: Any?,
        td: TypeDescriptor
    ): Any? {
        return convertIfNecessary(propertyName, oldValue, newValue, td.type, td)
    }

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

    private fun growArrayIfNecessary(array: Any, index: Int, name: String): Any {
        if (!autoGrowNestedPaths) {
            return array
        }
        val length = java.lang.reflect.Array.getLength(array)
        if (index in length until autoGrowCollectionLimit) {
            val componentType = array.javaClass.componentType

            // 创建一个更长的数组, 把元素拷贝过去
            val newArray = java.lang.reflect.Array.newInstance(componentType, index + 1)
            System.arraycopy(array, 0, newArray, 0, index)
            for (i in 0..index) {
                java.lang.reflect.Array.set(newArray, i, newValue(componentType, null, name))
            }
            setPropertyValue(name, newArray)
            return getPropertyValue(name) ?: throw IllegalStateException("Default value must not be null")
        } else {
            return array
        }
    }

    private fun growCollectionIfNecessary(
        collection: Collection<Any?>,
        index: Int,
        name: String,
        ph: PropertyHandler,
        nestingLevel: Int
    ) {
        if (!autoGrowNestedPaths || collection !is MutableCollection<*>) {
            return
        }
        if (index >= collection.size && index < autoGrowCollectionLimit) {
            val elementType: Class<*>? = Any::class.java  // TODO nesting not support
            if (elementType != null) {
                for (i in collection.size..index) {
                    (collection as MutableCollection<Any?>).add(newValue(elementType, null, name))
                }
            }
        }

    }

    /**
     * 为给定的Token去设置默认值
     *
     * @param tokens tokens
     * @return 为给定的token对应的属性去解析得到的默认值
     */
    private fun setDefaultValue(tokens: PropertyTokenHolder): Any {
        val propertyValue = createDefaultPropertyValue(tokens)
        setPropertyValue(tokens, propertyValue)
        return getPropertyValue(tokens) ?: throw IllegalStateException("Default value must not be null")
    }

    /**
     * 为给定的Token对应位置的属性, 去创建出来属性值[PropertyValue]
     *
     * @param tokens token
     * @return 创建出来的[PropertyValue]对象
     */
    private fun createDefaultPropertyValue(tokens: PropertyTokenHolder): PropertyValue {
        val td = getPropertyTypeDescriptor(tokens.canonicalName) ?: throw NullValueInNestedPathException(
            getRootClass(), this.nestedPath + tokens.canonicalName,
            "Could not determine property type for auto-growing a default value"
        )
        return PropertyValue(tokens.canonicalName, newValue(td.type, td, tokens.canonicalName))
    }

    /**
     * 为给定的类型, 去创建一个默认值
     *
     * @param type 要去进行创建对象的类型(可以是Array/Collection/Map等)
     * @param desc TypeDescriptor
     * @param name 属性名
     * @return 创建出来的默认值对象
     * @throws NullValueInNestedPathException 如果创建对象过程出现了异常
     */
    @Throws(NullValueInNestedPathException::class)
    private fun newValue(type: Class<*>, @Nullable desc: TypeDescriptor?, name: String): Any {
        try {
            if (type.isArray) {
                val componentType = type.componentType
                // TODO - only handles 2-dimensional arrays
                if (componentType.isArray) {
                    val array = java.lang.reflect.Array.newInstance(componentType, 1)
                    java.lang.reflect.Array.newInstance(componentType.componentType, 0)
                    return array
                } else {
                    return java.lang.reflect.Array.newInstance(componentType, 0)
                }
            } else if (ClassUtils.isAssignFrom(Collection::class.java, type)) {
                return CollectionFactory.createCollection<Any?>(type, 16)
            } else if (ClassUtils.isAssignFrom(Map::class.java, type)) {
                return CollectionFactory.createMap<Any?, Any?>(type, 16)
            } else {
                val declaredConstructor = type.getDeclaredConstructor()
                if (Modifier.isPublic(declaredConstructor.modifiers)) {
                    throw IllegalAccessException("Auto-growing not allowed with private constructor: $declaredConstructor")
                }
                return BeanUtils.instantiateClass(declaredConstructor)
            }
        } catch (ex: Throwable) {
            throw NullValueInNestedPathException(
                getRootClass(), this.nestedPath + name,
                "Could not instantiate property type [" + type.getName() + "] to auto-grow nested property path", ex
            )
        }
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
                    actualName = actualName ?: propertyName.substring(0, keyStart)

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

    @Nullable
    private fun convertIfNecessary(
        @Nullable propertyName: String?,
        @Nullable oldValue: Any?,
        @Nullable newValue: Any?,
        @Nullable requiredType: Class<*>?,
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

    /**
     * 执行对于propertyName当中存在有"[]"这样的Key表达式的情况的属性值设置
     *
     * @param tokens 解析完成得到的tokens
     * @param pv 要去进行设置的PropertyValue
     */
    private fun processKeyedProperty(tokens: PropertyTokenHolder, pv: PropertyValue) {
        // TODO
        val propValue = getPropertyHoldingValue(tokens)
        val ph = getLocalPropertyHandler(tokens.actualName)
            ?: throw InvalidPropertyException(
                getRootClass(),
                this.nestedPath + tokens.actualName,
                "No PropertyHandler found"
            )

        val lastKey = tokens.keys!![tokens.keys!!.size - 1]
        if (propValue.javaClass.isArray) {

        } else if (propValue is List<*>) {

        } else if (propValue is Map<*, *>) {

        }

    }

    /**
     * 获取该属性所持有的对象
     *
     * @param tokens tokens
     */
    private fun getPropertyHoldingValue(tokens: PropertyTokenHolder): Any {
        return Any()
    }

    /**
     * 处理本地的属性值的设置
     *
     * @param tokens token
     * @param pv 要去进行设置的属性值PropertyValue
     */
    private fun processLocalProperty(tokens: PropertyTokenHolder, pv: PropertyValue) {
        val ph = getLocalPropertyHandler(tokens.actualName)

        if (ph == null || !ph.writeable) {
            // TODO
            return
        }

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
         * 获取嵌套指定级别的[TypeDescriptor]
         *
         * @return TypeDescriptor for nested level propertyType
         */
        @Nullable
        abstract fun nested(level: Int): TypeDescriptor?

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