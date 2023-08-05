package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataElement
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils

/**
 * 描述这是一个用于完成Bean的实例化时需要使用到的构造器的参数列表;
 * * 1.支持添加通用的构造器参数, 使用type&name的方式去进行获取
 * * 2.支持添加indexed构造器参数, 按照index&type&name的方式去进行获取
 */
open class ConstructorArgumentValues() {
    /**
     * 通用的构造器参数列表
     */
    private val genericArgumentValues = ArrayList<ValueHolder>()

    /**
     * 带有index位置信息的构造器参数列表(Key-Index, Value-该位置的构造器参数信息)
     */
    private val indexedArgumentValues = LinkedHashMap<Int, ValueHolder>()

    constructor(constructorArgumentValues: ConstructorArgumentValues) : this() {
        this.addGenericArgumentValue(constructorArgumentValues)
    }

    /**
     * 将另外的一个[ConstructorArgumentValues]的构造器参数列表拷贝过来
     *
     * @param constructorArgumentValues 要去进行拷贝的[ConstructorArgumentValues]
     */
    open fun addArgumentValues(constructorArgumentValues: ConstructorArgumentValues) {
        constructorArgumentValues.genericArgumentValues.forEach { addOrMergeGenericArgumentValue(it) }
        constructorArgumentValues.indexedArgumentValues.forEach { addOrMergeIndexedArgumentValue(it.key, it.value) }
    }

    /**
     * 往[genericArgumentValues]当中添加一个参数
     *
     * @param value 需要添加的构造器参数值对象
     */
    open fun addGenericArgumentValue(value: Any) {
        this.genericArgumentValues += ValueHolder(value)
    }

    /**
     * 往[genericArgumentValues]当中添加一个参数
     *
     * @param value 需要添加的构造器参数值对象
     * @param type type
     */
    open fun addGenericArgumentValue(value: Any, type: String) {
        this.genericArgumentValues += ValueHolder(value, type)
    }

    /**
     * 往[genericArgumentValues]当中添加一个参数
     *
     * @param value 需要添加的构造器参数
     */
    open fun addGenericArgumentValue(value: ValueHolder) {
        this.genericArgumentValues += value
    }

    private fun addOrMergeGenericArgumentValue(value: ValueHolder) {
        if (!this.genericArgumentValues.contains(value)) {
            this.genericArgumentValues += value
        }
    }

    /**
     * 往[indexedArgumentValues]当中去添加一个参数
     *
     * @param index index
     * @param value value
     */
    open fun addIndexArgumentValue(index: Int, @Nullable value: Any?) {
        addIndexedArgumentValue(index, ValueHolder(value))
    }

    /**
     * 往[indexedArgumentValues]当中去添加一个参数
     *
     * @param index index
     * @param value value
     * @param type type
     */
    open fun addIndexArgument(index: Int, @Nullable value: Any?, @Nullable type: String?) {
        addIndexedArgumentValue(index, ValueHolder(value, type))
    }

    /**
     * 往[indexedArgumentValues]当中去添加一个参数
     *
     * @param index index
     * @param value 需要添加的构造器参数的ValueHolder
     */
    open fun addIndexedArgumentValue(index: Int, value: ValueHolder) {
        addOrMergeIndexedArgumentValue(index, value)
    }

    /**
     * 添加/合并[indexedArgumentValues]
     *
     * @param index index
     * @param valueHolder 需要添加的ValueHolder
     */
    private fun addOrMergeIndexedArgumentValue(index: Int, valueHolder: ValueHolder) {
        this.indexedArgumentValues[index] = valueHolder
    }

    /**
     * 检查[indexedArgumentValues]当中是否存在有给定的index位置的参数列表
     *
     * @param index index
     * @return 如果存在return true; 否则return false
     */
    open fun hasIndexedArgumentValue(index: Int): Boolean = indexedArgumentValues.containsKey(index)


    /**
     * 根据type从[genericArgumentValues]当中去获取到对应的参数值
     *
     * @param requiredType 需要的参数类型
     * @return 根据给定的参数类型去获取到的参数值(如果没有获取到的话, return null)
     */
    @Nullable
    open fun getGenericArgumentValue(requiredType: Class<*>): ValueHolder? {
        return getGenericArgumentValue(requiredType, null, null)
    }

    /**
     * 根据type和name从[genericArgumentValues]当中去获取到对应的参数值
     *
     * @param requiredType 需要的参数类型
     * @param requiredName 需要的参数名
     * @return 根据type和name获取到的参数值ValueHolder(如果没有获取到的话, return null)
     */
    @Nullable
    open fun getGenericArgumentValue(requiredType: Class<*>, requiredName: String): ValueHolder? {
        return getGenericArgumentValue(requiredType, requiredName, null)
    }

    /**
     * 根据type和name从[genericArgumentValues]当中去获取到对应的参数值
     *
     * @param requiredType 需要的参数类型(为null代表不匹配type)
     * @param requiredName 需要的参数名(为null代表不匹配name)
     * @param usedValueHolders 已经使用过的ValueHolder(可以为null), 对于已经使用过的那些, 我们不再进行二次使用
     * @return 根据type和name获取到的参数值ValueHolder(如果没有获取到的话, return null)
     */
    @Nullable
    open fun getGenericArgumentValue(
        @Nullable requiredType: Class<*>?,
        @Nullable requiredName: String?,
        @Nullable usedValueHolders: Set<ValueHolder>?
    ): ValueHolder? {
        this.genericArgumentValues.forEach { valueHolder ->
            // 如果该参数值已经用过了, 那么直接pass掉...
            if (usedValueHolders != null && usedValueHolders.contains(valueHolder)) {
                return@forEach
            }
            // 如果requiredName不为空, 那么就必须匹配name...
            if (requiredName != null && valueHolder.name != requiredName) {
                return@forEach
            }
            // 如果requiredType的typeName和simpleName都和type不匹配的话, 那么pass
            if (requiredType != null && valueHolder.type != requiredType.typeName && valueHolder.type != requiredType.simpleName) {
                return@forEach
            }

            // 检查ValueHolder的值类型和requiredType是否匹配?
            if (requiredType != null && valueHolder.value != null
                && ClassUtils.isAssignFrom(requiredType, valueHolder.value!!::class.java)
            ) {
                return@forEach
            }
            return valueHolder
        }
        return null
    }

    /**
     * 从[indexedArgumentValues]和[genericArgumentValues]当中根据index和type去获取到对应的构造器参数值
     *
     * @param index index
     * @param requiredType requiredType
     * @return 获取到的构造器参数值
     */
    @Nullable
    open fun getArgumentValue(index: Int, requiredType: Class<*>): ValueHolder? {
        return getArgumentValue(index, requiredType, null, null)
    }

    /**
     * 从[indexedArgumentValues]和[genericArgumentValues]当中根据index、type和name去获取到对应的构造器参数值
     *
     * @param index index
     * @param requiredType requiredType
     * @param requiredName requiredName
     * @return 获取到的构造器参数值
     */
    @Nullable
    open fun getArgumentValue(index: Int, requiredType: Class<*>, requiredName: String): ValueHolder? {
        return getArgumentValue(index, requiredType, requiredName, null)
    }

    /**
     * 从[indexedArgumentValues]和[genericArgumentValues]当中根据index、type和name去获取到对应的构造器参数值
     *
     * @param index index
     * @param requiredType requiredType
     * @param requiredName requiredName
     * @param usedValueHolders 已经用过的ValueHolder列表(已经使用过的, 不应该被重复使用)
     * @return 获取到的构造器参数值
     */
    @Nullable
    open fun getArgumentValue(
        index: Int,
        requiredType: Class<*>,
        requiredName: String?,
        usedValueHolders: Set<ValueHolder>?
    ): ValueHolder? {
        // 优先根据index去进行获取, 如果根据index无法获取, 那么尝试从genericArgument当中去进行获取
        return getIndexedArgumentValue(index, requiredType, requiredName)
            ?: getGenericArgumentValue(requiredType, requiredName, usedValueHolders)
    }

    /**
     * 根据index从[indexedArgumentValues]当中去获取到对应的参数列表
     *
     * @param index index
     * @param requiredName requiredName(可以为null)
     * @param requiredType requiredType(可以为null)
     */
    @Nullable
    open fun getIndexedArgumentValue(index: Int, requiredType: Class<*>?, requiredName: String?): ValueHolder? {
        val valueHolder = indexedArgumentValues[index] ?: return null

        // 如果requiredName不为空, 那么需要匹配valueHolder的name
        if (requiredName != null && valueHolder.name != requiredName) {
            return null
        }
        // 如果requiredType不为空, 那么需要匹配type是否匹配
        if (requiredType != null && requiredType.typeName != valueHolder.name && requiredType.simpleName != valueHolder.name) {
            return null
        }
        return valueHolder
    }

    /**
     * 获取所有的带有索引的参数列表
     *
     * @return 带有索引的构造器参数列表
     */
    open fun getIndexedArgumentValue(): Map<Int, ValueHolder> = LinkedHashMap(this.indexedArgumentValues)


    /**
     * 获取所有的通用的构造器列表
     *
     * @return 通用构造器列表
     */
    open fun getGenericArguments(): List<ValueHolder> = ArrayList(genericArgumentValues)

    /**
     * 获取参数数量
     *
     * @return 参数数量
     */
    open fun getArgumentCount(): Int = genericArgumentValues.size + indexedArgumentValues.size

    /**
     * 是否不存在有构造器参数?
     *
     * @return 如果两个列表当中都确实不存在的话, return true; 只要两者当中存在一个就会return false
     */
    open fun isEmpty(): Boolean = this.genericArgumentValues.isEmpty() && this.indexedArgumentValues.isEmpty()


    /**
     * clear当前的[ConstructorArgumentValues]
     */
    open fun clear() {
        this.genericArgumentValues.clear()
        this.indexedArgumentValues.clear()
    }


    class ValueHolder(
        @Nullable var value: Any?,
        @Nullable var type: String?,
        @Nullable var name: String?
    ) : BeanMetadataElement {

        /**
         * source
         */
        @Nullable
        private var source: Any? = null

        /**
         * 是否已经经过了类型转换?
         */
        private var converted = false

        /**
         * 经过转换之后的对象
         */
        @Nullable
        private var convertedValue: Any? = null

        constructor(@Nullable value: Any?) : this(value, null, null)

        constructor(@Nullable value: Any?, @Nullable type: String?) : this(value, type, null)

        /**
         * 设置已经完成类型转换之后的值
         *
         * @param value 转换之后的值
         */
        @Synchronized
        @Nullable
        fun setConvertedValue(@Nullable value: Any?) {
            this.converted = value != null
            this.convertedValue = value
        }

        /**
         * 获取转换之后的值
         *
         * @return 经过转换之后的值(如果不存在的话, return null)
         */
        @Synchronized
        @Nullable
        fun getConvertedValue(): Any? = this.convertedValue

        /**
         * 设置source
         *
         * @param source source
         */
        fun setSource(@Nullable source: Any?) {
            this.source = source
        }

        /**
         * 获取source
         *
         * @return source
         */
        @Nullable
        override fun getSource(): Any? {
            return source
        }

        /**
         * Copy出来得到一个新的ValueHolder
         *
         * @return new ValueHolder
         */
        fun copy(): ValueHolder {
            val valueHolder = ValueHolder(value, type, name)
            valueHolder.source = this.source
            return valueHolder
        }
    }
}