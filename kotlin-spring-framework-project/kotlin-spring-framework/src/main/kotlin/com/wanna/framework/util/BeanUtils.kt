package com.wanna.framework.util

import com.wanna.framework.beans.BeansException
import com.wanna.framework.beans.CachedIntrospectionResults
import com.wanna.framework.beans.FatalBeanException
import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.lang.Nullable
import java.beans.ConstructorProperties
import java.beans.PropertyDescriptor
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier

object BeanUtils {

    // 参数名发现器, 提供方法/构造器当中的参数名的获取
    private val parameterNameDiscoverer = DefaultParameterNameDiscoverer()

    // Key-包装类型, Value-基础类型
    private val primitiveWrapperTypeMap = LinkedHashMap<Class<*>, Class<*>>(8)

    // Key-基础类型, Value-包装类型
    private val primitiveTypeToWrapperMap = LinkedHashMap<Class<*>, Class<*>>(8)

    init {
        primitiveWrapperTypeMap[Int::class.javaObjectType] = Int::class.java
        primitiveWrapperTypeMap[Double::class.javaObjectType] = Double::class.java
        primitiveWrapperTypeMap[Short::class.javaObjectType] = Short::class.java
        primitiveWrapperTypeMap[Byte::class.javaObjectType] = Byte::class.java
        primitiveWrapperTypeMap[Long::class.javaObjectType] = Long::class.java
        primitiveWrapperTypeMap[Float::class.javaObjectType] = Float::class.java
        primitiveWrapperTypeMap[Char::class.javaObjectType] = Char::class.java
        primitiveWrapperTypeMap[Boolean::class.javaObjectType] = Boolean::class.java

        primitiveWrapperTypeMap.forEach { (k, v) -> primitiveTypeToWrapperMap[v] = k }
    }

    /**
     * 通过有参数构造器去创建对象, 需要指定构造器的参数列表
     *
     * @param ctor 构造器
     * @param args 参数列表
     * @return 实例化完成的对象
     */
    @JvmStatic
    fun <T> instantiateClass(ctor: Constructor<T>, vararg args: Any?): T {
        try {
            return ctor.newInstance(*args)
        } catch (ex: Exception) {
            ReflectionUtils.handleReflectionException(ex)
        }
        throw AssertionError("Cannot Reach Here!")
    }

    /**
     * 通过无参数构造器去创建对象
     *
     * @param ctor 无参构造器
     * @return 实例化完成的对象
     */
    @JvmStatic
    fun <T> instantiateClass(ctor: Constructor<T>): T {
        try {
            return ctor.newInstance()
        } catch (ex: Exception) {
            ReflectionUtils.handleReflectionException(ex)
        }
        throw AssertionError("Cannot Reach Here!")
    }

    /**
     * 通过无参数构造器创建对象
     *
     * @param clazz 想要去进行实例化的类
     * @param T 实例对象的类型
     * @return 实例化完成的对象
     */
    @JvmStatic
    fun <T> instantiateClass(clazz: Class<T>): T {
        try {
            val constructor = clazz.getDeclaredConstructor()
            constructor.isAccessible = true  // set Accessible
            return constructor.newInstance()
        } catch (ex: Exception) {
            ReflectionUtils.handleReflectionException(ex)
        }
        throw AssertionError("Cannot Reach Here!")
    }

    /**
     * 通过无参数构造器创建对象
     *
     * @param clazz 想要去进行实例化的类
     * @param T 实例化对象类型
     * @param assignTo 实例化对象类型
     * @return 实例化完成的对象
     *
     * @throws IllegalStateException 如果clazz实例无法转换成为T类型
     */
    @Throws(IllegalStateException::class)
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> instantiateClass(clazz: Class<*>, assignTo: Class<T>): T {
        if (!ClassUtils.isAssignFrom(assignTo, clazz)) {
            throw IllegalStateException("无法将[$clazz]去转换成为[$assignTo]类型")
        }
        try {
            val constructor = clazz.getDeclaredConstructor()
            constructor.isAccessible = true  // set Accessible
            return constructor.newInstance() as T
        } catch (ex: Exception) {
            ReflectionUtils.handleReflectionException(ex)
        }
        throw AssertionError("Cannot Reach Here!")
    }

    /**
     * 通过有参数构造器使用反射的方式, 去进行创建对象
     *
     * @param clazz 想要去进行实例化的类
     * @param parameterTypes 实例化时需要使用的构造器的参数类型列表
     * @param params 用来完成实例化时需要用到的参数列表
     * @return 利用给定的参数列表去进行实例化对象, 获取到实例化完成的对象
     */
    @JvmStatic
    fun <T> instantiateClass(clazz: Class<T>, parameterTypes: Array<Class<*>>, params: Array<Any?>): T {
        try {
            val constructor = clazz.getConstructor(*parameterTypes)
            constructor.isAccessible = true  // set Accessible
            return constructor.newInstance(*params)
        } catch (ex: Exception) {
            ReflectionUtils.handleReflectionException(ex)
        }
        throw AssertionError("Cannot Reach Here!")
    }

    /**
     * 获取一个构造器的参数名列表, 支持使用下面的两种策略去进行寻找：
     * * 1.如果存在有JDK当中提供的`@ConstructorProperties`注解, 那么从它上面去找;
     * * 2.如果没有`@ConstructorProperties`注解, 那么使用[DefaultParameterNameDiscoverer]去进行寻找
     *
     * @param ctor 要获取参数名的目标构造器
     * @throws IllegalStateException 如果没有找到合适的参数名列表/找到的参数名列表长度不对
     */
    @JvmStatic
    fun getParameterNames(ctor: Constructor<*>): Array<String> {
        val cp = ctor.getAnnotation(ConstructorProperties::class.java)
        val parameterNames: Array<String> = cp?.value ?: parameterNameDiscoverer.getParameterNames(ctor)
        ?: throw IllegalStateException("无法从目标构造器[ctor=$ctor]上获取到参数名列表")
        if (parameterNames.size != ctor.parameterCount) {
            throw IllegalStateException("匹配到的参数名的数量和目标构造器的参数数量不相同")
        }
        return parameterNames
    }

    /**
     * 判断给定的类, 是否是一个简单类型
     *
     * @param type 想要去进行判断的类
     * @return 如果是一个简单类型, return true; 否则return false
     */
    @JvmStatic
    fun isSimpleProperty(type: Class<*>): Boolean {

        // 如果是一个基础类型/基础雷系的包装类型, 那么return true
        if (isPrimitive(type) || isPrimitiveWrapper(type)) {
            return true
        }

        // 下面这些也被当做一个简单类型,
        if (type == Class::class.java || ClassUtils.isAssignFrom(
                CharSequence::class.java, type
            ) || ClassUtils.isAssignFrom(Number::class.java, type)
        ) {
            return true
        }
        return false
    }

    /**
     * 判断给定类clazz, 它是否是一个基础数据类型的Wrapper包装类(例如Integer/Double等)
     *
     * @param clazz 要进行判断的类型
     * @return 如果它是一个包装类型return true; 否则return false
     */
    @JvmStatic
    fun isPrimitiveWrapper(clazz: Class<*>): Boolean {
        return primitiveWrapperTypeMap.containsKey(clazz)
    }

    /**
     * 判断它是否是一个基础数据类型(int/double等)
     *
     * @param clazz 要进行判断的类型
     * @return 如果它是一个基础数据类型return true; 否则return false
     */
    @JvmStatic
    fun isPrimitive(clazz: Class<*>): Boolean {
        return primitiveTypeToWrapperMap.containsKey(clazz)
    }

    /**
     * 通过一个[Method], 去获取该方法对应的[PropertyDescriptor], 该方法必须是一个getter/setter
     *
     * @param method 要去寻找[PropertyDescriptor]的方法
     * @return 根据该方法(getter/setter), 去找到的[PropertyDescriptor], 如果找不到的话, return null
     */
    @Nullable
    @JvmStatic
    @Throws(BeansException::class)
    fun findPropertyForMethod(method: Method): PropertyDescriptor? {
        return findPropertyForMethod(method, method.declaringClass)
    }

    /**
     * 从给定的类当中, 去获取所有的属性, 检查给定的method是否是该属性的getter/setter?
     *
     * @param method method(getter/setter)
     * @param clazz 要去进行寻找[PropertyDescriptor]的类
     * @return 根据getter/setter方法去寻找到的属性[PropertyDescriptor], 如果找不到return null
     */
    @Nullable
    @JvmStatic
    @Throws(BeansException::class)
    fun findPropertyForMethod(method: Method, clazz: Class<*>): PropertyDescriptor? {
        val propertyDescriptors = getPropertyDescriptors(clazz)
        for (pd in propertyDescriptors) {
            // 如果它和readMethod/writeMethod其中一个匹配的话, return pd
            if (pd.readMethod == method || pd.writeMethod == method) {
                return pd
            }
        }
        return null
    }

    /**
     * 从给定的类当中, 去寻找到所有的JavaBeans PropertyDescriptor属性列表
     *
     * @param clazz 要去进行寻找属性的类
     * @return 从该类上寻找到的所有的属性列表
     */
    @JvmStatic
    @Throws(BeansException::class)
    fun getPropertyDescriptors(clazz: Class<*>): Array<PropertyDescriptor> {
        return CachedIntrospectionResults.forClass(clazz).getPropertyDescriptors()
    }

    /**
     * 从给定的Class当中, 根据propertyName去找到合适的属性的[PropertyDescriptor]
     *
     * @param clazz 需要去寻找属性的类
     * @param propertyName 属性名
     * @return 根据propertyName从clazz当中去寻找到的属性[PropertyDescriptor]
     */
    @Nullable
    @JvmStatic
    @Throws(BeansException::class)
    fun getPropertyDescriptor(clazz: Class<*>, propertyName: String): PropertyDescriptor? {
        return CachedIntrospectionResults.forClass(clazz).getPropertyDescriptor(propertyName)
    }

    /**
     * 将source对象当中的属性, 拷贝到target目标对象上去, 使用的方式是简单的浅拷贝的方式去进行的拷贝
     *
     * @param source source
     * @param target target
     */
    @JvmStatic
    @Throws(BeansException::class)
    fun copyProperties(source: Any, target: Any) {
        copyProperties(source, target, null)
    }

    /**
     * 将source对象当中的属性, 拷贝到target目标对象上去, 使用的方式是简单的浅拷贝的方式去进行的拷贝
     *
     * @param source source
     * @param target target
     */
    @JvmStatic
    @Throws(BeansException::class)
    fun copyProperties(source: Any, target: Any, editable: Class<*>) {
        copyProperties(source, target, editable, ignoreProperties = emptyArray())
    }

    /**
     * 将source对象当中的属性, 拷贝到target目标对象上去, 使用的方式是简单的浅拷贝的方式去进行的拷贝
     *
     * @param source source
     * @param target target
     * @param ignoreProperties 要去进行忽略拷贝的属性值...
     */
    @JvmStatic
    @Throws(BeansException::class)
    fun copyProperties(source: Any, target: Any, vararg ignoreProperties: String) {
        copyProperties(source, target, null, ignoreProperties = ignoreProperties)
    }

    /**
     * 将source对象当中的属性, 拷贝到target目标对象上去, 使用的方式是简单的浅拷贝的方式去进行的拷贝;
     * 对于实现copy的方式为, 使用source的getter的返回值, 使用target的setter去进行设置
     *
     * @param source source
     * @param target target
     * @param ignoreProperties 要去进行忽略的属性名列表
     *
     * @throws BeansException 如果拷贝属性的过程中出现了异常
     */
    @JvmStatic
    @Throws(BeansException::class)
    private fun copyProperties(
        source: Any,
        target: Any,
        @Nullable editable: Class<*>?,
        vararg ignoreProperties: String
    ) {
        // 计算得到要去获取Setter的目标类...
        var actualEditable: Class<*> = target.javaClass
        if (editable != null) {
            if (!editable.isInstance(target)) {
                throw IllegalArgumentException("Target class [${target.javaClass.name}] not assignable to Editable Class [${editable.name}]")
            }
            actualEditable = editable
        }

        // 获取target类身上的所有PropertyDescriptor
        val targetPds = getPropertyDescriptors(actualEditable)

        // 根据target类身上的全部Property, 尝试去进行逐一拷贝过来...
        for (targetPd in targetPds) {

            // 获取到target类身上的setter
            val writeMethod = targetPd.writeMethod

            // 如果target存在有setter, 并且该属性值并没有被排除, 那么需要对该属性去执行拷贝
            if (writeMethod != null && !ignoreProperties.contains(targetPd.name)) {

                // 从source类身上根据propertyName, 去进行获取到对应的getter
                val sourcePd = getPropertyDescriptor(source::class.java, targetPd.name)
                if (sourcePd != null) {
                    val readMethod = sourcePd.readMethod
                    if (readMethod != null
                        && ClassUtils.isAssignFrom(writeMethod.parameterTypes[0], readMethod.returnType)
                    ) {
                        try {
                            // 如果source类不是public的, 那么先去setAccessible
                            if (!Modifier.isPublic(readMethod.declaringClass.modifiers)) {
                                readMethod.isAccessible = true
                            }

                            // 使用getter获取到source object的对应属性值
                            val value = readMethod.invoke(source)

                            // 如果target类不是public的, 那么先去setAccessible
                            if (!Modifier.isPublic(writeMethod.declaringClass.modifiers)) {
                                writeMethod.isAccessible = true
                            }

                            // 使用setter去设置到target对象身上, 从而实现copy
                            writeMethod.invoke(target, value)
                        } catch (ex: Throwable) {
                            throw FatalBeanException(
                                "Could not copy property '${targetPd.name}' from source to target", ex
                            )
                        }
                    }
                }
            }
        }
    }

}