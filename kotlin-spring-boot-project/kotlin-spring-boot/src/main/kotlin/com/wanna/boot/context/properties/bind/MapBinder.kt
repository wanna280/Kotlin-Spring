package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.boot.context.properties.source.ConfigurationPropertySource
import com.wanna.boot.context.properties.source.IterableConfigurationPropertySource
import com.wanna.framework.core.CollectionFactory
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils

/**
 * 提供对于一个Map的聚合元素的属性绑定
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @param context Binder去进行属性绑定时用到的上下文信息
 */
class MapBinder(context: Binder.Context) : AggregateBinder<Map<Any?, Any?>>(context) {

    /**
     * 是否允许去进行递归绑定?
     *
     * @param source ConfigurationPropertySource
     */
    override fun isAllowRecursiveBinding(source: ConfigurationPropertySource?): Boolean = true

    /**
     * 执行对于一个Map这样的聚合元素的绑定
     *
     * @param name 对Map这样的聚合元素去进行绑定的属性Key前缀
     * @param target 对于待进行绑定的Map元素的相关描述信息
     * @param aggregateElementBinder 对于Map当中的单个元素的类型去提供递归的绑定的Callback回调函数
     * @return 对于Map的聚合元素去进行绑定的绑定结果(或者是null)
     */
    @Nullable
    override fun bindAggregate(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        aggregateElementBinder: AggregateElementBinder
    ): Any? {
        val mapType = if (target.value != null) Map::class.java else target.type.resolve(Any::class.java)
        // 根据mapType去创建出来Map对象
        val map = CollectionFactory.createMap<Any?, Any?>(mapType, 0)

        // 根据所有的ConfigurationPropertySource, 使用MapEntry的方式去进行绑定
        context.getSources().forEach {

            var source = it
            if (name != ConfigurationPropertyName.EMPTY) {
                // 如果可以获取到属性值的话, 那么直接去进行绑定
                val property = it.getConfigurationProperty(name)
                if (property != null) {
                    return context.getConverter().convert(property.value, target)
                }
                // 只需要前缀匹配的那些属性值...
                source = source.filter { name.isAncestorOf(it) }
            }

            // 使用MapEntry的Binder, 去将给定的ConfigurationPropertySource去完成绑定...
            EntryBinder(name, target, aggregateElementBinder).bindEntries(source, map)
        }
        return if (map.isEmpty()) null else map
    }

    /**
     * MapEntry的EntryBinder
     *
     * @param root 属性名前缀
     * @param target 要去进行绑定的目标Map聚合的元素的相关信息
     * @param aggregateElementBinder 对于Map当中的单个元素的类型去提供递归的绑定的Callback回调函数
     */
    private inner class EntryBinder(
        private val root: ConfigurationPropertyName,
        private val target: Bindable<Any>,
        private val aggregateElementBinder: AggregateElementBinder
    ) {
        /**
         * mapType
         */
        private val mapType: ResolvableType = target.type.asMap()

        /**
         * keyType
         */
        private val keyType: ResolvableType = mapType.getGenerics()[0]

        /**
         * valueType
         */
        private val valueType: ResolvableType = mapType.getGenerics()[1]

        /**
         * 将目标[ConfigurationPropertySource]当中的属性去绑定到map当中去
         *
         * @param source 正在去进行绑定的[ConfigurationPropertySource]
         * @param map 绑定的结果Map(绑定结果需要添加到这个Map当中去)
         */
        fun bindEntries(source: ConfigurationPropertySource, map: MutableMap<Any?, Any?>) {
            // 只有IterableConfigurationPropertySource才支持去进行Map的绑定
            // 因为我们需要遍历所有的propertyName, 尝试去进行找到合适的...
            if (source is IterableConfigurationPropertySource) {
                source.forEach { name ->
                    // 获取Value的相关信息Bindable
                    val valueBindable = getValueBindable(name)

                    // 获取EntryName, 也就是绑定时应该使用到属性Key前缀...
                    val entryName = getEntryName(source, name)

                    // 把entryName去切割掉root前缀得到keyName, 再利用Converter去对keyName去进行转换(正常情况不会改变)
                    val key = context.getConverter().convert<Any>(getKeyName(entryName), keyType)

                    // 对于Value的绑定结果去放入到map当中去, Key也就是keyName经过转换之后的结果(一般keyName只能为String)
                    // 对于Value的话, 使用AggregateElementBinder去对ValueBindable去进行绑定, 此时要去使用的propertyName=entryName
                    map.computeIfAbsent(key) { this.aggregateElementBinder.bind(entryName, valueBindable) }
                }
            }
        }

        /**
         * 将给定的[ConfigurationPropertyName]去转换成为字符串的Key的形式, 方便从Map当中去取值
         *
         * @param name 属性Key前缀
         * @return 去掉root之后得到的keyName
         */
        private fun getKeyName(name: ConfigurationPropertyName): String {
            val result = StringBuilder()
            for (index in root.getNumberOfElements() until name.getNumberOfElements()) {
                if (result.isNotEmpty()) {
                    result.append(".")
                }
                // fixed: 不管result是否为空, 都得添加name
                result.append(name.getElement(index))
            }
            return result.toString()
        }

        /**
         * 获取EntryName:
         * * 1.如果是Value是一个数组, 那么需要往后找到第一个类似"[0]"这样的index标志， 返回它之前的元素
         * * 2.如果Value是一个Object/Map, 那么需要从name当中切取, 相比root多一级去作为Key
         * * 3.如果Value是一个普通的值的话, 那么直接返回name
         *
         * @param source source
         * @param name 带获取EntryName的元素
         * @return entryName(其实也就是要去进行绑定的属性Key前缀)
         */
        private fun getEntryName(
            source: ConfigurationPropertySource,
            name: ConfigurationPropertyName
        ): ConfigurationPropertyName {
            val resolved = this.valueType.resolve(Any::class.java)

            // 如果Value的类型是Collection/Array, 那么从当前root的属性值开始, 往后去进行寻找, 找到第一个类似"[0]"这样的标志
            if (ClassUtils.isAssignFrom(Collection::class.java, resolved) || resolved.isArray) {
                // 比如"root=com.wanna", name="com.wanna.user[0]", 那么就需要返回"com.wanna.user"
                return chopNameAtNumericIndex(name)
            }

            // 1.如果name不是root的直接的parent节点, 并且Value不是一个简单的可以用Converter即可以去进行转换的对象
            // 2.如果name不是root的直接的parent节点, 并且Value是Object/Map类型(需要使用嵌套Map的方式来接收)的话,
            // 那么就需要从name去切取root往后一段去作为属性Key, 例如root="com.wanna", name="com.wanna.xxx.yyy", 那么此时就需要返回"com.wanna.xxx"
            // 其实总结来说就是: 如果Value是一个复杂的Java对象的话, Key只能多一级
            if (!this.root.isParentOf(name) && (isValueTreatedAsNestedMap() || !isScalarValue(source, name))) {
                return name.chop(this.root.getNumberOfElements() + 1)
            }

            // 如果name是root的直接parent节点, 那么
            return name
        }

        /**
         * 如果valueType是Object类型, 那么需要把它当做内部的Map的方式去进行解析
         *
         * @return 如果valueType是Object/Map类型, return true; 否则return false
         */
        private fun isValueTreatedAsNestedMap(): Boolean {
            return this.valueType.resolve(Any::class.java) == Any::class.java
        }

        /**
         * 检查Value值是否是标量值(valueType是一个Java的基础数据类型, Converter能进行转换的话, 就算是标量值)
         *
         * @param source source
         * @param name name
         * @return 如果它是一个标量值, return true; 否则return false
         */
        private fun isScalarValue(source: ConfigurationPropertySource, name: ConfigurationPropertyName): Boolean {
            val resolved = this.valueType.resolve(Any::class.java)
            // 如果value的类型, 不是以"java.lang"作为开始, 也不是一个枚举的话, 那么它一定不是一个标量值, return false
            if (!resolved.name.startsWith("java.lang") && !resolved.isEnum) {
                return false
            }
            // 如果在PropertySource当中不存在这样的一个属性值, 那么也不是一个标量值...
            val property = source.getConfigurationProperty(name) ?: return false

            // 利用占位符解析器, 去对propertyValue去进行占位符解析...
            val resolvedValue = context.getPlaceholderResolver().resolvePlaceholder(property.value)
            return context.getConverter().canConvert(resolvedValue, this.valueType)
        }

        /**
         * 从name当中根据index去进行切片, 切取得到合适的name
         *
         * @param name 正在去进行解析的配置属性Key
         * @return 解析得到以root作为前缀, 往后去进行寻找, 得到的第一个数字索引之前的全部element,
         * 比如"root=com.wanna", name="com.wanna.user[0]", 那么就需要返回"com.wanna.user"的[ConfigurationPropertyName]
         */
        private fun chopNameAtNumericIndex(name: ConfigurationPropertyName): ConfigurationPropertyName {
            val start = this.root.getNumberOfElements() + 1
            val end = name.getNumberOfElements()
            for (index in start until end) {
                // 如果该位置的元素是一个数字, 那么把它之前的全部元素, 去进行切割和返回
                if (name.isNumbericIndex(index)) {
                    return name.chop(index)
                }
            }
            return name
        }

        /**
         * 获取Value的绑定信息
         *
         * @param name 正在去进行绑定的属性名
         * @return Map元素的Value需要使用什么类型去进行绑定?
         */
        private fun getValueBindable(name: ConfigurationPropertyName): Bindable<Any> {
            // 如果Value是Object/Map类型, 并且root不是name的直接parent的话, 那么直接返回mapType
            if (!this.root.isParentOf(name) && isValueTreatedAsNestedMap()) {
                return Bindable.of(mapType)
            }
            // 如果Value不是Object/Map类型, 那么有可能是一个简单的类型String, 或者是一个复杂的Java对象类型, 这种情况需要去进行绑定的是valueType
            return Bindable.of(valueType)
        }

    }
}