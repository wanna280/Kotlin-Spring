package com.wanna.framework.util

import java.lang.reflect.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Java的反射工具类
 */
object ReflectionUtils {

    /**
     * 空的方法数组常量
     */
    @JvmStatic
    private val EMPTY_METHOD_ARRAY = emptyArray<Method>()

    /**
     * 空的类数组的常量
     */
    @JvmStatic
    private val EMPTY_CLASS_ARRAY = emptyArray<Class<*>>()

    /**
     * 空的字段的常量
     */
    @JvmStatic
    private val EMPTY_FIELD_ARRAY = emptyArray<Field>()

    /**
     * 某个类对应的方法缓存(ConcurrentMap)，k为要获取的类，v为该类所定义的方法列表
     */
    @JvmStatic
    private val declaredMethodsCache = ConcurrentHashMap<Class<*>, Array<Method>>()

    /**
     * 某个类对应的字段缓存(ConcurrentMap), k为要去进行获取的类, v为该类定义的字段列表
     */
    @JvmStatic
    private val declaredFieldsCache = ConcurrentHashMap<Class<*>, Array<Field>>()

    /**
     * 让一个字段变得可以访问
     *
     * @param field 目标字段
     */
    @JvmStatic
    @Suppress("DEPRECATION", "JDK9中isAccessible变为deprecated")
    fun makeAccessible(field: Field) {
        // 1.如果字段是final的
        // 2.如果字段不是public的
        // 3.如果字段所定义的类不是public的
        // 那么都得将字段的可见性变为true
        if ((Modifier.isFinal(field.modifiers) || !Modifier.isPublic(field.modifiers) || !Modifier.isPublic(field.declaringClass.modifiers)) && !field.isAccessible) {
            field.isAccessible = true
        }
    }


    /**
     * 让一个方法变得可以访问
     *
     * @param method 目标方法
     */
    @JvmStatic
    @Suppress("DEPRECATION", "JDK9中isAccessible变为deprecated")
    fun makeAccessible(method: Method) {
        // 1.如果方法不是public的
        // 2.如果类不是public的
        // 那么需要将该方法的可见性改为true
        if ((!Modifier.isPublic(method.modifiers) || !Modifier.isPublic(method.declaringClass.modifiers)) && !method.isAccessible) {
            method.isAccessible = true
        }
    }

    /**
     * 让一个构造器变得可以访问
     *
     * @param constructor 目标构造器
     */
    @JvmStatic
    @Suppress("DEPRECATION", "JDK9中isAccessible变为deprecated")
    fun makeAccessible(constructor: Constructor<*>) {
        // 1.如果构造器不是public的
        // 2.如果类不是public的
        // 那么需要将该构造器的可见性改为true
        if ((!Modifier.isPublic(constructor.modifiers) || !Modifier.isPublic(constructor.declaringClass.modifiers)) && !constructor.isAccessible) {
            constructor.isAccessible = true
        }
    }

    /**
     * 根据给定的异常[ex]去重新丢出来一个[RuntimeException]异常;
     * 如果原本就是一个[RuntimeException]或者是[Error], 那么直接抛出;
     * 如果不是[RuntimeException]或者[Error]的话, 那么把该异常去包装到[UndeclaredThrowableException]当中去进行抛出;
     *
     * @param ex 原始异常
     * @throws RuntimeException 转换之后的RuntimeException
     */
    @Throws(RuntimeException::class)
    fun rethrowRuntimeException(ex: Throwable) {
        when (ex) {
            is RuntimeException -> throw ex
            is Error -> throw ex
            else -> throw UndeclaredThrowableException(ex)
        }
    }

    /**
     * 根据给定的异常[ex]去重新丢出来一个[Exception]异常;
     * 如果原本就是一个[Exception]或者是[Error], 那么直接抛出;
     * 如果不是[Exception]或者[Error]的话, 那么把该异常去包装到[UndeclaredThrowableException]当中去进行抛出;
     *
     * @param ex 原始异常
     * @throws Exception 转换之后的Exception
     */
    @Throws(Exception::class)
    fun rethrowException(ex: Throwable) {
        when (ex) {
            is Exception -> throw ex
            is Error -> throw ex
            else -> throw UndeclaredThrowableException(ex)
        }
    }

    /**
     * 处理给定的执行目标方法异常[InvocationTargetException]
     *
     * @param ex 待处理的执行目标方法异常
     * @see rethrowRuntimeException
     */
    @JvmStatic
    fun handleInvocationTargetException(ex: InvocationTargetException) {
        rethrowRuntimeException(ex.targetException)
    }


    /**
     * 在给定的类上去找到一个给定字段名的字段
     *
     * @param clazz 需要去寻找字段的目标类
     * @param name 字段名
     * @return 如果找到了合适的字段, 返回对应的字段; 如果没有找到的话, return null
     */
    @JvmStatic
    fun findField(clazz: Class<*>, name: String): Field? {
        return findField(clazz, name, null)
    }

    /**
     * 在一个类上name和type都匹配的字段，如果没找到，return null
     *
     * @param clazz 目标类
     * @param name 字段名(如果name为空，那么类型匹配就行)
     * @param type 字段类型(如果type==null，那么匹配所有类型，找到就return)
     */
    @JvmStatic
    fun findField(clazz: Class<*>, name: String?, type: Class<*>?): Field? {
        var targetClass: Class<*>? = clazz
        do {
            val declaredFields = getDeclaredFields(targetClass!!, false)
            for (field in declaredFields) {
                // 如果type&name匹配的话，return
                if ((field.name == name && type == null)
                    || (name == null && field.type == type)
                    || (field.name == name && field.type == type)
                ) {
                    return field
                }
            }
            targetClass = targetClass.superclass
        } while (targetClass != null && targetClass != Any::class.java)
        return null
    }

    /**
     * 对一个类以及父类当中定义的所有字段，去执行某一个操作(action)
     *
     * @param clazz 目标类
     * @param action 对方法要执行的操作
     */
    @JvmStatic
    fun doWithFields(clazz: Class<*>, action: (Field) -> Unit) {
        doWithFields(clazz, action) { true }
    }

    /**
     * 对一个类以及父类当中定义的所有字段，去执行某一个操作(action)
     *
     * @param clazz 目标类
     * @param action 对方法要执行的操作
     * @param filter 哪些方法需要进行操作？使用Filter去进行过滤出来
     */
    @JvmStatic
    fun doWithFields(clazz: Class<*>, action: (Field) -> Unit, filter: (Field) -> Boolean) {
        var targetClass: Class<*>? = clazz
        do {
            doWithLocalFields(targetClass!!, action, filter)  // 执行当前的类中的所有字段
            targetClass = targetClass.superclass  // 向父类方向进行遍历
        } while (targetClass != null && targetClass != Any::class.java)
    }

    /**
     * 对一个类当中定义的所有字段，去执行某一个操作(action)
     *
     * @param clazz 目标类
     * @param action 对方法要执行的操作
     */
    @JvmStatic
    fun doWithLocalFields(clazz: Class<*>, action: (Field) -> Unit) {
        doWithLocalFields(clazz, action) { true }
    }

    /**
     * 对一个类当中定义的所有字段，去执行某一个操作(action)
     *
     * @param clazz 目标类
     * @param action 对方法要执行的操作
     * @param filter 哪些方法需要进行操作？
     */
    @JvmStatic
    fun doWithLocalFields(clazz: Class<*>, action: (Field) -> Unit, filter: (Field) -> Boolean) {
        val declaredFields = getDeclaredFields(clazz, false)
        declaredFields.filter(filter).forEach {
            try {
                action.invoke(it)
            } catch (ex: IllegalAccessException) {
                throw IllegalStateException("无法访问给定的字段[${it.name}]")
            }
        }
    }

    /**
     * 获取一个类定义的所有字段
     * @param clazz 目标类
     * @return 目标类定义的所有字段
     */
    @JvmStatic
    fun getDeclaredFields(clazz: Class<*>): Array<Field> {
        return getDeclaredFields(clazz, true)
    }

    /**
     * 获取一个类当中定义的所有字段
     *
     * @param clazz 目标类
     * @param defensive 是否具有侵入性的，如果defensive=true，那么需要克隆一份进行返回
     * @return 一个类当中的所有定义的字段列表
     */
    @JvmStatic
    fun getDeclaredFields(clazz: Class<*>, defensive: Boolean): Array<Field> {
        var result = declaredFieldsCache[clazz]
        // 如果从缓存当中获取不到，那么需要去构建declaredMethods
        if (result == null) {
            val declaredFields: Array<Field> = clazz.declaredFields
            declaredFieldsCache[clazz] = if (declaredFields.isEmpty()) EMPTY_FIELD_ARRAY else declaredFields
            result = declaredFields
        }
        // 如果有可能具有侵入性，那么需要clone一份进行return
        return if (defensive) result.clone() else result
    }

    /**
     * 获取某个字段的值
     *
     * @param field 字段
     * @param target 要获取的字段的目标对象
     */
    @JvmStatic
    fun getField(field: Field, target: Any): Any? {
        return field[target]
    }

    /**
     * 设置某个字段的值
     *
     * @param field 要进行设置的字段
     * @param target 要设置的目标对象
     * @param value 该字段即将要设置的值
     */
    @JvmStatic
    fun setField(field: Field, target: Any, value: Any?) {
        field[target] = value
    }

    /**
     * 根据name去指定类当中找到无参数的方法，有可能没找到，return null
     *
     * @param clazz 目标类
     * @param name 方法name
     */
    @JvmStatic
    fun findMethod(clazz: Class<*>, name: String): Method? {
        return findMethod(clazz, name, *EMPTY_CLASS_ARRAY)
    }

    /**
     * 根据name和参数类型列表去指定类当中去寻找方法，有可能没找到，return null；
     * 会尝试去搜索所有的父类当中的所有方法，去进行匹配，直到，方法名和参数列表都完全匹配时，return
     *
     * @param clazz 目标类
     * @param name 方法name
     * @param parameterTypes 方法的参数类型
     */
    @JvmStatic
    fun findMethod(clazz: Class<*>, name: String, vararg parameterTypes: Class<*>): Method? {
        var searchType: Class<*>? = clazz
        do {
            val methods = if (searchType!!.isInterface) searchType.methods else getDeclaredMethods(searchType, false)
            methods.forEach {
                if (it.name == name && parameterTypes.contentEquals(it.parameterTypes)) {
                    return it
                }
            }
            searchType = searchType.superclass
        } while (searchType != null && searchType != Any::class.java)
        return null
    }

    /**
     * 反射执行目标方法
     * @param method 目标方法
     * @param target 目标方法要传递的this对象
     */
    @JvmStatic
    fun invokeMethod(method: Method, target: Any?): Any? {
        return invokeMethod(method, target, *emptyArray())
    }

    /**
     * 使用Java反射的方式，去执行给定的目标方法
     *
     * @param method 目标方法
     * @param target 目标方法要传递的this对象
     * @param args 执行目标方法需要的参数列表，在拿到参数之后，会是以数组的方式去进行获取，但是method.invoke时
     * 又是传递的vararg，就导致了数组里套数组的情况，需要使用*将数组中的元素继续进行拆分开
     * @return 执行目标方法返回的对象
     */
    @JvmStatic
    fun invokeMethod(method: Method, target: Any?, vararg args: Any?): Any? {
        try {
            return method.invoke(target, *args)
        } catch (ex: Exception) {
            handleReflectionException(ex)
        }
        throw IllegalStateException("不应该到达这里！")
    }

    /**
     * 处理反射过程当中可能会产生的异常
     *
     * @param ex 要去处理的异常
     */
    @JvmStatic
    fun handleReflectionException(ex: Exception) {
        if (ex is NoSuchMethodException) {
            throw IllegalStateException("要执行的目标方法没有找到:[${ex.message}]")
        }
        if (ex is InvocationTargetException) {
            handleInvocationTargetException(ex)
        }
        if (ex is IllegalAccessException) {
            throw IllegalStateException("不合法的访问一个方法/字段:[${ex.message}]")
        }
        if (ex is RuntimeException) {
            throw ex
        }
        throw UndeclaredThrowableException(ex)
    }

    /**
     * 判断一个方法是否是equals方法
     *
     * @param method 要进行判断的方法
     */
    @JvmStatic
    fun isEqualsMethod(method: Method): Boolean {
        // 判断方法名和参数数量和参数类型
        return method.name == "equals" && method.parameterCount == 1 && method.parameterTypes[0] == Any::class.java
    }

    /**
     * 判断一个方法是否是toString方法
     *
     * @param method 要进行判断的方法
     */
    @JvmStatic
    fun isToStringMethod(method: Method): Boolean {
        return method.name == "toString" && method.parameterCount == 0
    }

    /**
     * 判断一个方法是否是hashCode方法
     *
     * @param method 要进行判断的方法
     */
    @JvmStatic
    fun isHashCodeMethod(method: Method): Boolean {
        return method.name == "hashCode" && method.parameterCount == 0
    }

    /**
     * 判断这个方法是否来自于Object类，可以重写的equals/hashCode/toString方法也需要进行判断
     *
     * @param method 要进行判断的方法
     * @return 如果它是Object的方法, 那么return true; 否则return false
     */
    @JvmStatic
    fun isObjectMethod(method: Method): Boolean {
        return method.declaringClass == Any::class.java || isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(
            method
        )
    }

    /**
     * 判断给定的字段是否是"public static final"的字段？
     *
     * @param field 待检查的字段
     * @return 如果是"public static final", return true; 否则return false
     */
    @JvmStatic
    fun isPublicStaticFinal(field: Field): Boolean {
        return Modifier.isPublic(field.modifiers) && Modifier.isStatic(field.modifiers) && Modifier.isFinal(field.modifiers)
    }

    /**
     * 对一个类当中定义的所有方法，执行同样的操作
     *
     * @param clazz 要执行方法的类
     * @param action 要根据Method去进行执行的操作
     */
    @JvmStatic
    fun doWithLocalMethods(clazz: Class<*>, action: (Method) -> Unit) {
        doWithLocalMethods(clazz, action) { true }
    }

    /**
     * 对一个类当中定义的所有方法，执行同样的操作
     *
     * @param clazz 要执行方法的类
     * @param action 要根据Method去进行执行的操作
     * @param filter 该方法是否要执行的Filter？return true->执行，else->不执行
     */
    @JvmStatic
    fun doWithLocalMethods(clazz: Class<*>, action: (Method) -> Unit, filter: (Method) -> Boolean) {
        val declaredMethods = getDeclaredMethods(clazz, false)
        declaredMethods.filter(filter).forEach {
            try {
                action.invoke(it)
            } catch (ex: IllegalAccessException) {
                throw IllegalStateException("无法访问给定的方法[${it.name}]")
            }
        }
    }

    /**
     * 对一个类以及它父类当中定义的所有方法，执行同样的操作
     *
     * @param clazz 要执行方法的类
     * @param action 要根据Method去进行执行的操作
     */
    @JvmStatic
    fun doWithMethods(clazz: Class<*>, action: (Method) -> Unit) {
        doWithMethods(clazz, action) { true }
    }

    /**
     * 对一个类以及它父类当中定义的所有方法，执行同样的操作
     *
     * @param clazz 要执行方法的类
     * @param action 要根据Method去进行执行的操作
     * @param filter 该方法是否要执行的Filter？return true->执行，else->不执行
     */
    @JvmStatic
    fun doWithMethods(clazz: Class<*>, action: (Method) -> Unit, filter: (Method) -> Boolean) {
        val declaredMethods = getDeclaredMethods(clazz, false)
        declaredMethods.filter(filter).forEach {
            try {
                action.invoke(it)
            } catch (ex: IllegalAccessException) {
                throw IllegalStateException("无法访问给定的方法[${it.name}]")
            }
        }

        // 如果它还有父类，并且父类不是Object(Any)的话，那么，让它的父类也执行这个操作
        if (clazz.superclass != null && clazz.superclass != Any::class.java) {
            doWithMethods(clazz.superclass, action, filter)

            // 如果它是个接口了，那么也把它当做类去执行方法
        } else if (clazz.isInterface) {
            doWithMethods(clazz, action, filter)
        }
    }

    /**
     * 获取一个类当中定义的所有的方法，包括所有的父类方法
     *
     * @param clazz 要匹配的类
     * @return 该类当中的所有定义的方法列表
     */
    @JvmStatic
    fun getAllDeclaredMethods(clazz: Class<*>): Array<Method> {
        val allDeclaredMethods = ArrayList<Method>()
        // 因为doWithMethods，正好会遍历所有的方法，正好利用该方法...
        doWithMethods(clazz, allDeclaredMethods::add)
        return allDeclaredMethods.toTypedArray()
    }

    /**
     * 获取一个类定义的所有方法, 因为有可能具有侵入性, 会改变缓存中的declaredMethod数组, 因此clone一份出来return, 通过defensive去进行实现
     *
     * @param clazz 要获取方法的类
     * @return 该方法当中定义的方法列表
     */
    @JvmStatic
    fun getDeclaredMethods(clazz: Class<*>): Array<Method> {
        return getDeclaredMethods(clazz, true)
    }

    /**
     * 获取一个类定义的所有方法
     *
     * @param clazz 要获取方法的类
     * @param defensive 这个方法是否具有侵入性？也就是需不需要将数据clone一份出来返回？true代表需要，反之不需要
     * @return 从给定的类上去解析完成的方法数组
     */
    @JvmStatic
    fun getDeclaredMethods(clazz: Class<*>, defensive: Boolean): Array<Method> {
        // 首先尝试，从缓存当中获取该类定义的方法，获取不到，那么去进行构建...
        var result = declaredMethodsCache[clazz]
        if (result == null) {
            val declaredMethods: Array<Method> = clazz.declaredMethods
            val defaultMethods: List<Method> = findConcreteMethodsOnInterfaces(clazz)
            // 将declaredMethod列表和defaultMethod列表全部拷贝到result当中，并放入到缓存当中(如果为空的话，需要放入一个空方法的常量值)
            // Array，第二个参数是传递的数组的index->Array[index]的元素产生方法的callback
            result = Array(declaredMethods.size + defaultMethods.size) { index ->
                if (index < declaredMethods.size) declaredMethods[index] else defaultMethods[index - declaredMethods.size]  // fixed: index->index - declaredMethods.size
            }
            declaredMethodsCache[clazz] = if (result.isEmpty()) EMPTY_METHOD_ARRAY else result
        }
        // 如果defensive=true，需要clone一份进行返回，如果defensive=false，那么直接返回原始对象
        return if (defensive) result.clone() else result
    }

    /**
     * 在一个类的所有接口上去上寻找Concrete(具体的，已经进行实现的)方法，也就是default方法
     *
     * @param clazz 需要去进行寻找方法的类
     * @return 如果没有找到default方法，那么只是返回一个空的list，而不是null
     */
    @JvmStatic
    private fun findConcreteMethodsOnInterfaces(clazz: Class<*>): List<Method> {
        val defaultMethods: MutableList<Method> = ArrayList()
        clazz.interfaces.forEach {
            it.methods.forEach { method ->
                if (!Modifier.isAbstract(method.modifiers)) {
                    defaultMethods.add(method)
                }
            }
        }
        return defaultMethods
    }
}