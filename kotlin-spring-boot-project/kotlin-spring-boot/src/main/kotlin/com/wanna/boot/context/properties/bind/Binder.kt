package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.*
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import org.slf4j.LoggerFactory
import java.util.function.Supplier

/**
 * Binder, 提供将属性值去绑定到Java对象上的支持
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 *
 * @param sources PropertySources, 提供数据绑定的属性来源, 以[ConfigurationPropertySource]的方式给出
 * @param placeholdersResolver 提供占位符的解析的解析器(可以为null, 使用默认值, 不提供占位符的解析功能)
 * @param conversionServices ConversionService列表, 提供属性的绑定时的类型转换功能的相关支持
 * @param bindHandler 默认的BindHandler(可以为null, 使用默认值)
 * @param constructorProvider 提供需要去进行绑定的构造器的Provider(可以为null, 使用默认值), 主要是提供ValueObject的绑定
 *
 * @see ConfigurationPropertySource
 * @see ConfigurationPropertySources.from
 * @see BindHandler
 * @see BindConstructorProvider
 */
class Binder(
    private val sources: Iterable<ConfigurationPropertySource>,
    @Nullable placeholdersResolver: PlaceholdersResolver?,
    conversionServices: List<ConversionService>,
    @Nullable bindHandler: BindHandler?,
    @Nullable constructorProvider: BindConstructorProvider?
) {
    /**
     * 默认的BindHandler
     */
    private val defaultBindHandler: BindHandler = bindHandler ?: BindHandler.DEFAULT

    /**
     * 占位符的解析
     */
    private val placeholdersResolver: PlaceholdersResolver = placeholdersResolver ?: PlaceholdersResolver.NONE

    /**
     * 提供要去进行绑定的构造器的Provider
     */
    private val constructorProvider: BindConstructorProvider = constructorProvider ?: BindConstructorProvider.DEFAULT

    /**
     * DataObject的绑定器, 提供对于一个Java对象的绑定, 主要包含两个DataObjectBinder:
     * * 1.ValueObject的Binder, 针对的那些不可变的元素的绑定, 采用构造器的方式去进行注入, 最终完成绑定, 和Kotlin当中的ValueObject的定义相同
     * * 2.JavaBean的Binder, 主要是针对那些可变元素的绑定 ,基于JavaBean的Getter/Setter的方式去进行实现并完成绑定
     */
    private val dataObjectBinders = listOf(ValueObjectBinder(this.constructorProvider), JavaBeanBinder.INSTANCE)

    /**
     * 提供绑定属性的[BindConverter], 内部组合了一些[ConversionService]去协助完成绑定
     */
    private val bindConverter: BindConverter = BindConverter.get(conversionServices)

    /**
     * 将给定的属性值的前缀的所有属性值去绑定到目标对象上(如果之前还没有实例对象的话, 那么绑定失败)
     *
     * @param name 需要绑定的配置文件的属性名的前缀
     * @param target 待绑定的目标对象相关信息的Bindable(将会把name作为的前缀信息的配置信息, 全部都绑定到这个Bindable当中)
     * @return 属性绑定完成的结果BindResult
     */
    fun <T : Any> bind(name: String, target: Bindable<T>): BindResult<T> {
        val bind = bind(ConfigurationPropertyName.of(name), target, defaultBindHandler, false)
        return BindResult.of(bind)
    }

    /**
     * 将给定的属性值的前缀的所有属性值去绑定到目标对象上(如果之前还没有实例对象的话, 那么根据type去进行实例化)
     *
     * @param name  需要绑定的配置文件的属性名的前缀
     * @param target 待绑定的目标对象相关信息的Bindable(将会把name作为的前缀信息的配置信息, 全部都绑定到这个Bindable当中)
     * @return 绑定完成的实例对象(Note: 如果是Binder内部新创建的对象, 那么不会去进行属性绑定...)
     */
    @Nullable
    fun <T : Any> bindOrCreate(name: String, target: Bindable<T>): T? {
        return bindOrCreate(ConfigurationPropertyName.of(name), target, null)
    }

    /**
     * 将给定的属性值的前缀的所有属性值去绑定到目标对象上(如果之前还没有实例对象的话, 那么根据type去进行实例化)
     *
     * @param name  需要绑定的配置文件的属性名的前缀
     * @param target 待绑定的目标对象相关信息的Bindable(将会把name作为的前缀信息的配置信息, 全部都绑定到这个Bindable当中)
     * @param bindHandler 监听绑定的过程当中的各个事件触发的监听器
     * @return 绑定完成的实例对象(Note: 如果是Binder内部新创建的对象, 那么不会去进行属性绑定...)
     */
    @Nullable
    fun <T : Any> bindOrCreate(name: String, target: Bindable<T>, @Nullable bindHandler: BindHandler?): T? {
        return bindOrCreate(ConfigurationPropertyName.of(name), target, bindHandler)
    }

    /**
     * 将给定的属性值的前缀的所有属性值去绑定到目标对象上(如果之前还没有实例对象的话, 那么根据type去进行实例化)
     *
     * @param name  需要绑定的配置文件的属性名的前缀
     * @param target 待绑定的目标对象相关信息的Bindable(将会把name作为的前缀信息的配置信息, 全部都绑定到这个Bindable当中)
     * @param bindHandler 监听绑定的过程当中的各个事件触发的监听器
     * @return 绑定完成的实例对象(Note: 如果是Binder内部新创建的对象, 那么不会去进行属性绑定...)
     */
    @Nullable
    fun <T : Any> bindOrCreate(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        @Nullable bindHandler: BindHandler?
    ): T? {
        return bind(name, target, bindHandler, true)
    }

    /**
     * 将给定的属性值的前缀的所有属性值去绑定到目标对象上(如果之前还没有实例对象的话, 那么可以尝试根据type去进行实例化)
     *
     * @param name  需要绑定的配置文件的属性名的前缀
     * @param target 待绑定的目标对象相关信息的Bindable(将会把name作为的前缀信息的配置信息, 全部都绑定到这个Bindable当中)
     * @param bindHandler 监听绑定的过程当中的各个事件触发的监听器
     * @param create 如果之前还没没实例对象的话, 是否需要根据type去进行创建
     * @return 绑定完成的实例对象(Note: 如果是Binder内部新创建的对象, 那么不会去进行属性绑定...)
     */
    @Nullable
    private fun <T : Any> bind(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        @Nullable bindHandler: BindHandler?,
        create: Boolean
    ): T? {
        return bind(name, target, bindHandler ?: defaultBindHandler, Context(), false, create)
    }

    /**
     * 将给定的属性值的前缀的所有属性值去绑定到目标对象上(如果之前还没有实例对象的话, 那么可以尝试根据type去进行实例化)
     *
     * @param name  需要绑定的配置文件的属性名的前缀
     * @param target 待绑定的目标对象相关信息的Bindable(将会把name作为的前缀信息的配置信息, 全部都绑定到这个Bindable当中)
     * @param bindHandler 监听绑定的过程当中的各个事件触发的监听器
     * @param context 进行属性绑定过程中需要用到的上下文信息
     * @param allowRecursiveBinding 是否允许去进行递归的绑定?
     * @param create 如果之前还没没实例对象的话, 是否需要根据type去进行创建
     * @return 绑定完成的实例对象(Note: 如果是Binder内部新创建的对象, 那么不会去进行属性绑定...)
     */
    @Nullable
    private fun <T : Any> bind(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        bindHandler: BindHandler,
        context: Context,
        allowRecursiveBinding: Boolean,
        create: Boolean
    ): T? {
        var bindTarget: Bindable<T> = target
        try {
            // 给BindHandler一个机会, 去对要去进行绑定的Bindable去进行替换, 如果return null, 直接结束绑定
            bindTarget = bindHandler.onStart(name, target, context)
                ?: return handleBindResult(name, target, bindHandler, context, null, create)

            // 执行绑定工作
            val bound = bindObject(name, bindTarget, bindHandler, context, allowRecursiveBinding)

            // 处理绑定结果
            return handleBindResult(name, bindTarget, bindHandler, context, bound, create)
        } catch (ex: Exception) {
            // 处理绑定出错的情况
            return handleBindError(name, bindTarget, bindHandler, context, ex)
        }
    }

    /**
     * 执行真正的绑定工作
     *
     * (1)如果可以找到目标属性Key前缀对应的ConfigurationProperty, 那么使用ConfigurationProperty去进行绑定
     *
     * @param name 属性Key前缀
     * @param target 要去进行绑定的目标类型信息
     * @param handler 用于去监听属性绑定的监听器
     * @param context 属性绑定的上下文信息
     * @param allowRecursiveBinding 是否允许对同一个类去进行递归绑定?
     * @return 针对Bindable去执行的绑定的绑定结果(可能为null)
     */
    @Nullable
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> bindObject(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        handler: BindHandler,
        context: Context,
        allowRecursiveBinding: Boolean
    ): Any? {
        // 根据属性名, 从PropertySource列表当中去找到合适的ConfigurationProperty
        val property = findProperty(name, target, context)

        // 如果找不到对应的属性值, 并且深度不为0的话, 并且已经确定了确实一定是不存在有这样的name作为前缀对应的子属性的话, 那么pass掉...return null
        if (property == null && context.getDepth() != 0 && containsNoDescendantOf(context.getSources(), name)) {
            return null
        }

        // 如果要去进行绑定的类型是Map/Array/Collection的话, 需要使用聚合的绑定器去进行绑定...
        val aggregateBinder = getAggregateBinder(target as Bindable<Any>, context)
        if (aggregateBinder != null) {
            return bindAggregate(name, target, handler, context, aggregateBinder)
        }

        // 如果匹配上了普通的ConfigurationProperty类型的话, 那么使用ConfigurationProperty去进行绑定
        if (property != null) {
            return bindProperty(target, context, property)
        }

        // 如果获取不到对应的属性的话, 那么使用DataObject的方式去进行绑定...
        return bindDataObject(name, target as Bindable<Any>, handler, context, allowRecursiveBinding)
    }

    /**
     * 从各个[ConfigurationPropertySource]当中去寻找到合适的用于去进行绑定的属性值
     *
     * @param name 属性名
     * @param target 正在绑定的对象
     * @param context Binder绑定的上下文参数
     * @return 从[ConfigurationPropertySource]当中去获取到的属性值, 获取不到return null
     */
    @Nullable
    private fun <T : Any> findProperty(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        context: Context
    ): ConfigurationProperty? {
        if (name.isEmpty()) {
            return null
        }
        // 从各个ConfigurationPropertySource当中去寻找到对应的属性值
        context.getSources().forEach {
            val property = it.getConfigurationProperty(name)
            if (property != null) {
                return property
            }
        }
        return null
    }

    /**
     * 检查给定的[ConfigurationPropertySource]列表当中, 是否不存在有给定的[name]对应的子属性?
     *
     * @param sources sources列表
     * @param name 属性Key前缀
     */
    private fun containsNoDescendantOf(
        sources: Iterable<ConfigurationPropertySource>,
        name: ConfigurationPropertyName
    ): Boolean {
        sources.forEach {
            // 只要不是ABSENT, 那么都认为该PropertySource当中存在有name作为前缀的配置信息
            if (it.containsDescendantOf(name) != ConfigurationPropertyState.ABSENT) {
                return false
            }
        }
        // 如果全部都是ABSENT, 那么return true
        return true
    }

    /**
     * 处理最终的绑定的结果
     *
     * @param name 属性名
     * @param target 正在去进行绑定的对象的相关信息
     * @param handler handler
     * @param context 绑定的上下文信息
     * @param result 绑定的结果的实例对象(可能为null)
     * @param create 如果绑定的时候, 没有合适的对象的话, 是否需要去进行创建?
     * @return 处理完成的绑定结果的对象
     */
    @Nullable
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> handleBindResult(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        handler: BindHandler,
        context: Context,
        result: Any?,
        create: Boolean
    ): T? {
        var result: Any? = result

        // 如果之前就已经有实例对象了, 那么在这里去进行类型转换即可
        if (result != null) {
            result = handler.onSuccess(name, target as Bindable<Any>, context, result)
            result = context.getConverter().convert(result, target)
        }

        // 如果之前还没有实例对象的话, 那么就在这里去进行创建
        if (result == null && create) {
            result = create(target as Bindable<Any>, context)
            result = handler.onCreate(name, target, result)
            result = context.getConverter().convert(result, target)
        }
        handler.onFinish(name, target as Bindable<Any>, context, result)
        return result as T?
    }

    private fun <T : Any> handleBindError(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        handler: BindHandler,
        context: Context,
        ex: Exception
    ): T? {
        throw ex
    }

    private fun create(target: Bindable<Any>, context: Context): Any? {
        this.dataObjectBinders.forEach {
            val instance = it.create(target, context)
            if (instance != null) {
                return instance
            }
        }
        return null
    }

    /**
     * 执行单个属性值的绑定
     *
     * @param target 待绑定的目标对象
     * @param context 属性绑定的上下文信息
     * @param property 正在去进行绑定的属性值
     * @return 绑定属性值的结果(可能为null)
     */
    @Nullable
    private fun <T : Any> bindProperty(
        target: Bindable<T>,
        context: Context,
        property: ConfigurationProperty
    ): Any? {
        // 设置正在去进行绑定的属性值到Context当中...
        context.setConfigurationProperty(property)
        var result: Any? = property.value

        // 对于解析到的要去进行绑定的值, 去进行解析占位符
        result = placeholdersResolver.resolvePlaceholder(result)

        // 利用Converter去进行类型转换
        result = context.getConverter().convert(result, target)
        return result
    }

    /**
     * 检查是否不需要去进行继续绑定了?
     *
     * @param name 正在去进行绑定的属性Key的前缀
     * @param target 要去进行绑定的目标对象的相关信息
     * @param context 绑定属性的上下文信息
     */
    private fun isUnbindableBean(name: ConfigurationPropertyName, target: Bindable<Any>, context: Context): Boolean {
        // 检查所有的PropertySource
        context.getSources().forEach {
            // 如果我们明知道有要去进行绑定的属性, 我们就不能去进行跳过
            if (it.containsDescendantOf(name) == ConfigurationPropertyState.PRESENT) {
                return false
            }
        }
        val type = target.type.resolve(Any::class.java)

        // 对于基础数据类型/Object/Class, 都不需要去进行绑定...
        if (type.isPrimitive || type == Any::class.java || type == Class::class.java) {
            return true
        }
        // 对于以java开头的类, 都不需要去进行绑定
        return type.name.startsWith("java.")
    }

    /**
     * 使用DataObject的方式去绑定属性值, 对于一个Java对象当中的所有属性去进行绑定
     *
     * @param name 属性Key的前缀
     * @param target 要去进行绑定的目标对象的Bindable
     * @param handler 监听绑定过程的监听器
     * @param context 属性绑定的上下文参数信息
     * @param allowRecursiveBinding 是否允许去进行递归绑定同一个类?
     * @return 针对给定的要去进行绑定的Bindable, 去绑定得到的绑定结果(有可能为null)
     */
    @Nullable
    private fun bindDataObject(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        handler: BindHandler,
        context: Context,
        allowRecursiveBinding: Boolean
    ): Any? {
        // 如果正在绑定的是java的内部的类/Object类/Class类, 那么直接pass掉, 这些类型是递归的结束终止条件...
        if (isUnbindableBean(name, target, context)) {
            return null
        }
        // 获取到目标对象的类型
        val type = target.type.resolve(Any::class.java)

        // 如果不允许递归绑定, 并且当前类已经在DataObject的绑定栈当中了, 那么别去进行继续绑定了, return null
        if (!allowRecursiveBinding && context.isDataObjectBinding(type)) {
            return null
        }

        // 获取对单个的属性值去进行绑定的DataObjectPropertyBinder
        // 对于PropertyBinder的target, 是属性值的target, 也就是基于一个对象的具体字段去进行的描述信息
        // 一个字段来说, 应该也需要使用bind方法去进行绑定, 其实也就是递归绑定, 对于一个对象内部的定义的对象, 都支持进行递归的绑定
        val propertyBinder = DataObjectPropertyBinder { propertyName, propertyTarget ->
            // Note:
            // (1)对于某一个字段来说, 它需要绑定的配置文件的前缀为"{name}.{propertyName}", 因此这里需要生成出来子属性名...
            // (2)这里需要去进行绑定的是某个属性的Bindable(propertyTarget), 而不是原始的target...
            bind(name.append(propertyName), propertyTarget, handler, false)
        }

        // 利用所有的DataObjectBinder, 尝试去对target去使用JavaBean的绑定方式进行绑定
        return context.withDataObject(type) {
            for (dataObjectBinder in this.dataObjectBinders) {
                val instance = dataObjectBinder.bind(name, target, context, propertyBinder)
                if (instance != null) {
                    return@withDataObject instance
                }
            }
            return@withDataObject null
        }
    }

    /**
     * 使用聚合的Binder去对目标对象去进行绑定
     *
     * @param name 属性前缀
     * @param target 要去进行绑定的目标类型信息
     * @param handler 监听绑定流程的监听器
     * @param context 属性绑定的上下文信息
     * @param aggregateBinder 聚合绑定的Binder
     * @return 执行聚合绑定的绑定结果
     */
    @Suppress("UNCHECKED_CAST")
    @Nullable
    private fun <T : Any> bindAggregate(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        handler: BindHandler,
        context: Context,
        aggregateBinder: AggregateBinder<T>
    ): Any? {
        // 构建一个聚合元素的Binder, 去提供聚合元素的属性值绑定, 聚合时需要使用的属性前缀为具体的propertyName, bindTarget为propertyTarget
        val aggregateElementBinder = AggregateElementBinder { propertyName, propertyTarget, source ->
            val allowRecursiveBinding = aggregateBinder.isAllowRecursiveBinding(source)
            val supplier = Supplier {
                // 这里也是执行Binder的bind方法, 执行递归绑定...
                bind(propertyName, propertyTarget, handler, context, allowRecursiveBinding, false)
            }
            context.withSource(source, supplier)
        }
        // 在增加depth的情况下, 去执行Supplier, 利用AggregateElementBinder去进行绑定
        return context.withIncreasedDepth {
            aggregateBinder.bind(name, target as Bindable<Any>, aggregateElementBinder)
        }
    }

    /**
     * 如果要去进行绑定的类型是Map/Collection/Array的情况, 需要构建出来对应的聚合Binder去进行绑定
     *
     * @param target 要去进行绑定的目标类型
     * @param context 绑定的上下文信息
     * @return 用于对Map/Collection/Array去进行绑定的聚合Binder(如果不是Map/Collection/Array类型的话, return null)
     */
    @Suppress("UNCHECKED_CAST")
    @Nullable
    private fun getAggregateBinder(target: Bindable<Any>, context: Context): AggregateBinder<Any>? {
        val type = target.type.resolve(Any::class.java)
        if (ClassUtils.isAssignFrom(Map::class.java, type)) {
            return MapBinder(context) as AggregateBinder<Any>
        } else if (ClassUtils.isAssignFrom(Collection::class.java, type)) {
            return CollectionBinder(context)
        } else if (ClassUtils.isAssignFrom(Array::class.java, type)) {
            return ArrayBinder(context)
        }
        return null
    }

    /**
     * 维护Binder对于属性值的绑定过程当中的上下文信息
     */
    inner class Context constructor() : BindContext {
        /**
         * 正在去进行绑定的属性值, 对应的是配置文件的前缀, 比如"xxx.yyy.zzz"
         */
        private var property: ConfigurationProperty? = null

        /**
         * 属性绑定的深度,例如: A内部有B对象, B内部有C对象, 此时在递归调用时, 就需要记录一下深度...
         */
        private var depth = 0

        /**
         * 维护正在去进行绑定的DataObject的绑定的类的队列, 用于检查是否在进行递归绑定同一个类
         */
        private val dataObjectBindings = ArrayDeque<Class<*>>()

        /**
         * 维护正在去执行构造器绑定的队列
         */
        private val constructorBindings = ArrayDeque<Class<*>>()

        /**
         * Source的栈的深度
         */
        private var sourcePushCount: Int = 0

        /**
         * Source栈
         */
        private val source = ArrayList<ConfigurationPropertySource>(listOf())

        /**
         * 将给定的Class压入到要去进行构造器绑定的type的队列当中
         *
         * @param value value
         */
        fun pushConstructorBoundTypes(value: Class<*>) = this.constructorBindings.add(value)

        /**
         * 将正在去进行构造器绑定的队列当中的最后一个Class元素弹出
         */
        fun popConstructorBoundTypes() = this.constructorBindings.removeLastOrNull()

        /**
         * 是否是构造器的嵌套绑定?
         *
         * @return 如果是构造器的嵌套绑定return true; 否则return false
         */
        fun isNestedConstructorBinding(): Boolean = constructorBindings.isNotEmpty()

        /**
         * 使用DataObject的方式去进行绑定
         *
         * @param type type, 使用栈的方式去记录一下当前正在进行绑定的对象类型
         * @param supplier Supplier
         */
        fun <T : Any?> withDataObject(type: Class<*>, supplier: Supplier<T>): T {
            this.dataObjectBindings.add(type)
            try {
                return withIncreasedDepth(supplier)
            } finally {
                dataObjectBindings.removeLastOrNull()
            }
        }

        /**
         * 在增加调用的深度的情况下, 去进行Supplier的执行
         *
         * @param supplier Supplier
         * @return Supplier当中维护的对象
         */
        fun <T : Any?> withIncreasedDepth(supplier: Supplier<T>): T {
            increaseDepth()
            try {
                return supplier.get()
            } finally {
                decreaseDepth()
            }
        }

        /**
         * 在记录Source的情况下, 去进行Supplier的执行
         *
         * @param source source
         * @param supplier Supplier
         * @return Supplier当中维护的对象
         */
        fun <T : Any?> withSource(source: ConfigurationPropertySource?, supplier: Supplier<T>): T {
            source ?: return supplier.get()
            sourcePushCount++
            this.source.add(0, source)
            try {
                return supplier.get()
            } finally {
                sourcePushCount--
            }
        }

        /**
         * 检查当前对象是否正在被递归绑定?
         *
         * @param type 需要去进行检查的type
         * @return 如果在dataObjectBinding栈当中已经存在type的话, return true; 否则return false
         */
        fun isDataObjectBinding(type: Class<*>): Boolean = this.dataObjectBindings.contains(type)

        private fun increaseDepth() {
            this.depth++
        }

        private fun decreaseDepth() {
            this.depth--
        }


        override fun getBinder(): Binder = this@Binder

        override fun getDepth(): Int = this.depth

        override fun getSources(): Iterable<ConfigurationPropertySource> = this@Binder.sources

        fun setConfigurationProperty(property: ConfigurationProperty?) {
            this.property = property
        }

        fun clearConfigurationProperty() {
            this.property = null
        }

        override fun getConfigurationProperty(): ConfigurationProperty? = this.property

        /**
         * 获取占位符的解析器
         *
         * @return 占位符解析器
         */
        fun getPlaceholderResolver(): PlaceholdersResolver = this@Binder.placeholdersResolver

        /**
         * 获取用于提供类型转换的的BindConverter, 内部通过ConversionService去协助从而实现绑定
         *
         * @return BindConverter
         */
        fun getConverter(): BindConverter = this@Binder.bindConverter
    }

    companion object {

        /**
         * Logger
         */
        private val logger = LoggerFactory.getLogger(Binder::class.java)

        /**
         * 根据环境信息去构建出来一个Binder对象
         *
         * @param environment Environment
         * @return Binder
         */
        @JvmStatic
        fun get(environment: Environment): Binder {
            return get(environment, null)
        }

        /**
         * 根据环境信息和BindHandler去构建出来一个Binder对象
         *
         * @param environment environment
         * @param bindHandler BindHandler, 用于监听各个绑定的事件
         * @return Binder
         */
        @JvmStatic
        fun get(environment: Environment, @Nullable bindHandler: BindHandler?): Binder {
            // 根据Environment, 去创建出来ConfigurationPropertySource列表
            val sources = ConfigurationPropertySources.get(environment)
            // 创建一个基于Environment去提供占位符的解析的PlaceholdersResolver
            val placeholdersResolver = PropertySourcesPlaceholdersResolver(environment)

            // 构建出来Binder
            return Binder(sources, placeholdersResolver, emptyList(), bindHandler, null)
        }
    }
}