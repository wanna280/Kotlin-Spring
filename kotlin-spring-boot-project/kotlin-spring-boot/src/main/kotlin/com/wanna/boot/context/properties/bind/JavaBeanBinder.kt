package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.lang.Nullable
import com.wanna.framework.beans.BeanUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.common.logging.LoggerFactory
import java.beans.Introspector
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.BiConsumer
import java.util.function.Supplier

/**
 * JavaBean的Binder, 基于JavaBean的Getter&Setter的方式去进行提供JavaBean的属性绑定功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
open class JavaBeanBinder : DataObjectBinder {

    companion object {
        /**
         * JavaBeanBinder单例对象
         */
        @JvmStatic
        val INSTANCE = JavaBeanBinder()

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(JavaBeanBinder::class.java)
    }

    /**
     * 对一个给定的配置文件的属性Key前缀, 去进行绑定
     * (但是因为我们这里是对全部的BeanProperty去进行绑定, 因此我们用不到ConfigurationPropertyName;
     * 对于创建子属性名, 比如现在是"xxx.yyy", JavaBean有一个字段为"zzz", 我们其实是需要ConfigurationPropertyName的,
     * 实际上ConfigurationPropertyName是会被DataObjectPropertyBinder所持有的, 这样才能保证递归过程当中, 始终前缀不会丢失)
     *
     * @param name 待进行绑定的属性名前缀(配置文件的前缀)
     * @param target 要去进行绑定的对象目标Bindable(可能是来自一个正常的Java对象, 也可能是来自于一个BeanProperty的Getter字段)
     * @param context Binder去进行绑定属性时用到的上下文信息
     * @param propertyBinder 对单个属性去提供绑定的PropertyBinder
     * @return 绑定完成的实例对象(或者是绑定失败return null)
     */
    override fun <T : Any> bind(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        context: Binder.Context,
        propertyBinder: DataObjectPropertyBinder
    ): T? {
        // 根据Bindable的相关信息, 我们去构建出来一个Bean对象
        val bean = Bean.get(target, true) ?: return null

        // 使用Getter去获取到已经存在的Java对象, 或者是使用无参数构造器对一个字段值去进行初始化...
        val beanSupplier = bean.getSupplier(target)

        // 执行真正的绑定工作, 遍历所有的BeanProperty, 去执行逐一绑定...
        val bound = bind(propertyBinder, bean, beanSupplier, context)

        // 如果其中一个字段绑定成功, 那么返回绑定成功的对象; 否则return null
        return if (bound) beanSupplier.get() else null
    }

    /**
     * 根据目标[Bindable]的对象类型, 使用无参数构造器去完成实例化
     *
     * @param target 待完成绑定的目标Bindable
     * @param context 用于属性绑定的上下文参数信息
     * @return 根据Bindable去创建完成的实例对象
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> create(target: Bindable<T>, context: Binder.Context): T? {
        val type = target.type.resolve()
        return if (type == null) null else BeanUtils.instantiateClass(type) as T
    }

    /**
     * 对于给定的[Bean]上的各个BeanProperty字段, 去进行逐一的绑定
     *
     * @param propertyBinder 提供属性绑定的PropertyBinder
     * @param bean 正在绑定的Bean
     * @param beanSupplier BeanSupplier
     * @param context BindingContext
     * @return 是否绑定成功? (只要其中一个字段绑定成功, 那么就算是绑定成功)
     */
    private fun <T : Any> bind(
        propertyBinder: DataObjectPropertyBinder,
        bean: Bean<T>,
        beanSupplier: BeanSupplier<T>,
        context: Binder.Context
    ): Boolean {
        var bound = false

        // 遍历当前JavaBean当中的所有的BeanProperty, 去进行逐一绑定
        for (beanProperty in bean.properties.values) {

            // 对当前的BeanProperty去进行绑定(Note: 这里的或运算, 如果把bound放在前面的话, 会直接阻断后面的运行)
            bound = bind(beanSupplier, propertyBinder, beanProperty) || bound

            // clear ConfigurationProperty
            context.clearConfigurationProperty()
        }
        return bound
    }

    /**
     * 对于一个目标BeanProperty, 利用BeanProperty的Setter去提供属性绑定的功能
     *
     * @param beanSupplier 提供单例Bean的BeanSupplier
     * @param propertyBinder 提供属性绑定的PropertyBinder
     * @param property 待进行绑定的BeanProperty
     * @return 如果绑定成功, 那么return true; 如果绑定失败, return false
     */
    private fun <T : Any> bind(
        beanSupplier: BeanSupplier<T>,
        propertyBinder: DataObjectPropertyBinder,
        property: BeanProperty
    ): Boolean {
        // 获取到目标属性值
        val name = property.getName()

        // 获取到该属性值的类型, 这里是带泛型的(通过Setter/Getter去获取到的类型)
        val type = property.getType()

        // 使用Getter, 去获取到该BeanProperty在绑定之前的属性值,
        // 因为很多情况下, 在绑定之前就已经有了默认值了...
        val value = property.getValue(beanSupplier)

        // 直接从字段上去进行寻找, 获取到属性值上的注解信息, 获取不到就算了...
        val annotations = property.getAnnotations() ?: emptyArray()

        // 针对待进行绑定的字段BeanProperty, 去构建出来一个Bindable对象, 去提供属性的绑定
        val bound = propertyBinder.bindProperty(
            name,
            Bindable.of<Any>(type).withSuppliedValue(value).withAnnotations(*annotations)
        ) ?: return false

        // 如果该BeanProperty当中存在有Setter, 那么使用Setter去进行注入
        if (property.isSettable()) {
            property.setValue(beanSupplier, bound)

            // 如果没有Setter, 也没有Getter, 那么不允许存在...
            // 如果没有Setter, 有Getter, 但是绑定的值和原始的值不一致的话, 那么是不合法的
            // 比如原来通过Getter获取到的值是1, 但是现在通过PropertyBinder去计算得到该属性值应该是2, 但是这个时候, 你却没有Setter, Spring不允许这种情况存在
        } else if (value == null || bound != value.get()) {
            throw IllegalStateException("没有为目标属性值[${property.getName()}]去找到合适的Setter")
        }
        return true
    }

    /**
     * 对于一个JavaBean的描述, 内部存在有多个属性值
     *
     * @param type JavaBean类型(带泛型的ResolvableType)
     * @param resolvedType JavaBean类型(Class)
     * @param T JavaBean类型
     */
    class Bean<T>(private val type: ResolvableType, private val resolvedType: Class<*>) {

        companion object {

            /**
             * 判断给定的类是否是可以去进行实例化的?
             *
             * @param type type
             * @return 如果它是接口, 或者是不含有无参数构造器, 那么return false; 否则return true
             */
            @JvmStatic
            fun isInstantiable(type: Class<*>): Boolean {
                if (type.isInterface) {
                    return false
                }
                try {
                    type.getDeclaredConstructor()
                    return true
                } catch (ex: Throwable) {
                    return false
                }
            }

            /**
             * 根据Bindable去获取用于去进行属性的绑定的Bean
             *
             * @param bindable Bindable
             * @param canCallGetValue 是否需要调用get方法去获取到实例对象, 从而获取到实例类型, 从而去推断实例类型?
             * @return Bean(内部封装了一个类当中的各个属性的Getter/Setter/Field信息), 如果解析得到的类型无法实例化, return null
             */
            @JvmStatic
            @Nullable
            @Suppress("UNCHECKED_CAST")
            fun <T : Any> get(bindable: Bindable<T>, canCallGetValue: Boolean): Bean<T>? {
                val value = bindable.value as Supplier<T?>?
                val type = bindable.type

                // 先尝试根据给定的ResolvableType去获取到解析的Class, 解析不到的话, 那么默认为Object
                var resolvedType = type.resolve(Any::class.java)
                var instance: T? = null

                // 如果支持调用get方法的话, 那么获取到真实的对象类型, 去解析得到Class
                if (canCallGetValue && value != null) {
                    instance = value.get()
                    resolvedType = instance?.javaClass ?: resolvedType
                }
                // 如果该类型无法去完成实例化, 那么return null
                if (instance == null && !isInstantiable(resolvedType)) {
                    return null
                }
                return Bean(type, resolvedType)
            }
        }

        /**
         * 维护了一个JavaBean当中的各个属性值(Getter&Setter&Field);
         * Key是属性名, Value是该属性对应的BeanProperty(BeanProperty的name将会被转换成为破折号连接的形式)
         */
        val properties = LinkedHashMap<String, BeanProperty>()

        init {
            // 构建出来BeanProperty列表
            addProperties(resolvedType)
        }

        /**
         * 根据给定的Bindable去获取到当前类型的Bean的BeanSupplier
         *
         * @param target 正在去进行绑定的字段信息Bindable(value对应的是Getter的Supplier)
         * @return 用于获取到该字段对应的JavaBean实例的BeanSupplier
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> getSupplier(target: Bindable<T>): BeanSupplier<T> {
            return BeanSupplier {
                var instance: Any? = null
                // 如果target Bindable当中存在有Supplier的话, 通过Supplier拿到对象...
                // 这里的Supplier.get的调用, 其实相当于是调用了Getter方法去获取到字段上的原本的默认值
                // 也就说, 在递归绑定时, 如果该字段已经有了对象了, 那么我们直接沿用该对象去进行绑定...
                if (target.value != null) {
                    instance = target.value.get()
                }

                // 如果该字段的默认值为null, 那么我们使用无参数构造器去进行实例化...
                // 也就是说, 在递归绑定时, 我们是允许默认值是null的, 我们根据无参构造器去进行实例化即可
                if (instance == null) {
                    instance = BeanUtils.instantiateClass(resolvedType)
                }
                return@BeanSupplier instance as T
            }
        }


        /**
         * 根据beanType以及它的所有的父类当中的属性信息, 去构建出来BeanProperty列表
         *
         * @param  type beanType
         */
        @Suppress("UNCHECKED_CAST")
        private fun addProperties(type: Class<*>) {
            var clazz: Class<*>? = type
            // 遍历type以及它的所有的父类, 去添加BeanProperty
            while (clazz != null && clazz != Any::class.java) {
                val declaredMethods = ReflectionUtils.getDeclaredMethods(clazz)
                val declaredFields = ReflectionUtils.getDeclaredFields(clazz)

                // 根据当前类的所有的字段和方法, 去添加BeanProperty
                addProperties(declaredMethods as Array<Method?>, declaredFields)
                clazz = clazz.superclass
            }
        }

        /**
         * 根据BeanType的类当中定义的所有的方法和字段的添加BeanProperty
         *
         * @param declaredMethods 类当中定义的方法列表
         * @param declaredFields 类当中定义的字段列表
         */
        private fun addProperties(declaredMethods: Array<Method?>, declaredFields: Array<Field>) {
            // 把不合法的方法去clear掉, 使用null去进行填充, 后续去进行处理时, 就不会再去处理该位置的方法了...
            for (index in declaredMethods.indices) {
                if (!isCandidateMethod(declaredMethods[index])) {
                    declaredMethods[index] = null
                }
            }

            // 先根据"isXXX"的方法去构建一遍BeanProperty, 并把它加入到Getter当中
            for (method in declaredMethods) {
                addMethodIfPossible(method, "is", 0, BeanProperty::addGetter)
            }
            // 再根据"getXXX"的方法去构建一遍BeanProperty, 并把它加入到Getter当中(如果之前已经存在有isXXX, 那么需要去进行替换)
            for (method in declaredMethods) {
                addMethodIfPossible(method, "get", 0, BeanProperty::addGetter)
            }
            // 最后根据"setXXX"的方法去构建一遍BeanProperty当中, 把它加入到Setter当中
            for (method in declaredMethods) {
                addMethodIfPossible(method, "set", 1, BeanProperty::addSetter)
            }

            // 根据所有的字段, 去加入到现在已经存在的BeanProperty当中的field字段当中去
            // 如果之前没有合适的Getter/Setter, 那么该字段就不需要去进行保存
            for (field in declaredFields) {
                addField(field)
            }
        }

        /**
         * 判断给定的方法是否有可能是一个候选的Getter/Setter方法
         *
         * @param method 待匹配的方法
         * @return 如果确实是不合法的Getter/Setter, 那么return false; 否则return true
         */
        private fun isCandidateMethod(method: Method?): Boolean {
            // 1.如果是private/protected/static, 或者它是一个桥接方法, 那么都pass掉...
            // 2.如果它是来自于Object/Class类的方法, 那么pass掉...
            // 3.如果一个方法名当中含有$, 说明它是一个合成方法, 那么pass调
            return method != null
                    && !Modifier.isPrivate(method.modifiers) && !Modifier.isProtected(method.modifiers)
                    && !Modifier.isStatic(method.modifiers) && !method.isBridge
                    && method.declaringClass != Any::class.java && method.declaringClass != Class::class.java
                    && !method.name.contains("$")
        }

        /**
         * 添加字段Field到已经存在有的BeanProperty当中
         *
         * @param field 待添加的字段
         */
        private fun addField(field: Field) {
            this.properties[field.name]?.addField(field)
        }

        /**
         * 如果必要的话, 添加一个方法到BeanProperty当中去
         *
         * @param method method, 待添加的方法(可以为null)
         * @param prefix 期望的方法名前缀(get/is/set)
         * @param parameterCount 期望的方法参数数量
         * @param consumer consumer(BeanProperty会根据方法名去进行获取/创建, Method就是给定的方法)
         */
        private fun addMethodIfPossible(
            @Nullable method: Method?,
            prefix: String,
            parameterCount: Int,
            consumer: BiConsumer<BeanProperty, Method>
        ) {
            // 如果参数数量和期望的参数数量相同, 并且方法名以给定的prefix作为前缀的话, 那么该方法就需要去进行添加
            if (method != null && method.parameterCount == parameterCount && method.name.startsWith(prefix) && method.name.length > prefix.length) {
                // 去掉前缀, 首字母小写, 得到属性名, 比如setName, 变成name
                val propertyName = Introspector.decapitalize(method.name.substring(prefix.length))

                // 如果之前已经存在有该属性名的BeanProperty的话, 那么直接回调Consumer
                // 如果之前还不存在该属性名的BeanProperty的话, 那么先创建出来一个BeanProperty, 再回调Consumer
                consumer.accept(this.properties.computeIfAbsent(propertyName, this::getBeanProperty), method)
            }
        }

        /**
         * 根据属性名去构建出来BeanProperty
         *
         * @param name 属性名
         * @return 构建出来的BeanProperty
         */
        private fun getBeanProperty(name: String) = BeanProperty(name, type)
    }

    /**
     * 用于对Bean去进行实例化的Supplier,在Supplier的基础上,
     * 去新增提供了缓存功能, 如果之前已经完成过实例的创建, 那么直接从缓存当中去进行获取,
     * 避免因为多次调用get方法, 从而创建出来了不同的对象实例
     *
     * @param factory 用于对Bean去进行实例化的Supplier
     */
    class BeanSupplier<T : Any>(private val factory: Supplier<T>) : Supplier<T> {

        /**
         * Bean实例对象
         */
        private var instance: T? = null

        /**
         * 获取到Bean实例
         *
         * @return Bean实例对象
         */
        override fun get(): T {
            if (this.instance == null) {
                this.instance = factory.get()
            }
            return this.instance!!
        }
    }

    /**
     * 描述的是一个JavaBean的属性值, 包括Getter/Setter/Field等
     *
     * @param name 属性名(后续会转换成为dashed的风格, 也就是使用破折号的方式去进行连接的方式)
     * @param declaringClassType 待去进行绑定的JavaBean的类型
     */
    class BeanProperty(name: String, private val declaringClassType: ResolvableType) {

        /**
         * 将原始的属性名去转换成为dashed风格, 使用'-'的方式去进行连接
         */
        private val name = DataObjectPropertyName.toDashedForm(name)

        /**
         * Getter方法
         */
        @Nullable
        private var getter: Method? = null

        /**
         * Setter方法
         */
        @Nullable
        private var setter: Method? = null

        /**
         * Getter/Setter具体的字段
         */
        @Nullable
        private var field: Field? = null

        /**
         * 获取当前属性值的类型(如果有Setter, 那么就使用Setter的第一个参数作为属性值类型; 如果没有Setter, 那么就使用Getter的返回值类型去作为属性值类型)
         *
         * @return 属性值的类型
         */
        fun getType(): ResolvableType {
            // 如果Setter不为null, 那么就使用Setter的第一个参数去作为属性值的类型
            if (this.setter != null) {
                return ResolvableType.forMethodParameter(MethodParameter(this.setter!!, 0))
            }
            // 如果Setter为null, 那么就得使用Getter的返回值去作为属性值的类型
            return ResolvableType.forMethodParameter(MethodParameter(this.getter!!, -1))
        }

        /**
         * 返回当前BeanProperty的属性名
         *
         * @return name
         */
        fun getName(): String = this.name

        /**
         * 如果之前还没初始化过getter, 或者之前的getter是以is开头的, 那么就需要设置getter
         *
         * @param getter getter
         */
        fun addGetter(getter: Method) {
            if (this.getter == null || isBetterGetter(getter)) {
                this.getter = getter
            }
        }

        /**
         * 检查给定的Getter方法是否是一个更好的Getter?
         * (如果之前的getter是以is开头的, 那么当前getter就比之前的好; 说明getXXX方法的优先级比isXXX方法的优先级高)
         *
         * @param getter getter
         * @return 如果给定的getter比当前的getter更好的话, 那么return true; 否则return false
         */
        private fun isBetterGetter(getter: Method): Boolean {
            return this.getter != null && this.getter!!.name.startsWith("is")
        }

        /**
         * 如果之前还没初始化过setter, 或者给定的setter和之前已经完成初始化的getter的参数和返回值类型匹配的话, 需要初始化setter
         *
         * @param setter setter
         */
        fun addSetter(setter: Method) {
            if (this.setter == null || isBetterSetter(setter)) {
                this.setter = setter
            }
        }

        /**
         * 判断给定的setter是否之前的Setter更好?(如果当前的setter的第一个参数和之前的getter的返回值匹配的话, 那么就是一个更好的setter)
         *
         * @param setter setter
         * @return 如果当前的setter比之前的好, 那么return true; 否则return false
         */
        private fun isBetterSetter(setter: Method): Boolean {
            return this.getter != null && this.getter!!.returnType == setter.parameterTypes[0]
        }

        /**
         * 为当前的BeanProperty的字段去完成初始化
         *
         * @param field field
         */
        fun addField(field: Field) {
            if (this.field == null) {
                this.field = field
            }
        }

        /**
         * 获取当前BeanProperty的注解信息
         *
         * @return 当前BeanProperty的字段上的注解信息
         */
        @Nullable
        fun getAnnotations(): Array<Annotation>? {
            return try {
                field?.declaredAnnotations
            } catch (ex: Throwable) {
                null
            }
        }

        /**
         * 判断当前的BeanProperty是否支持去进行值的设置
         *
         * @return 如果有setter, return true; 否则return false
         */
        fun isSettable(): Boolean {
            return this.setter != null
        }

        /**
         * 获取当前的BeanProperty的值
         *
         * @param instance 属性值的实例对象
         * @return 使用Getter获取到的属性值的Supplier
         */
        @Nullable
        fun getValue(instance: Supplier<*>): Supplier<Any>? {
            this.getter ?: return null
            return Supplier {
                try {
                    this.getter!!.isAccessible = true
                    return@Supplier this.getter!!.invoke(instance.get())
                } catch (ex: Throwable) {
                    throw IllegalStateException("无法根据Getter去为[$name]去获取属性值", ex)
                }
            }
        }

        /**
         * 给当前的BeanProperty去进行值的设置
         *
         * @param instance 需要去获取属性值的实例对象
         * @param value 需要去对当前的BeanProperty去设置成为什么值?
         */
        fun setValue(instance: Supplier<*>, @Nullable value: Any?) {
            try {
                this.setter!!.isAccessible = true
                this.setter!!.invoke(instance.get(), value)
            } catch (ex: Throwable) {
                throw IllegalStateException("无法使用Setter去为[$name]这个属性去设置值", ex)
            }
        }

        override fun toString(): String = "$declaringClassType.$name"
    }
}