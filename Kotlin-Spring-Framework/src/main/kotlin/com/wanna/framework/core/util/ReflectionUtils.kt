package com.wanna.framework.core.util

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Java的反射工具类
 */
class ReflectionUtils {

    companion object {

        /**
         * 空的方法/类/字段数组的常量标识符
         */
        @JvmField
        val EMPTY_METHOD_ARRAY = emptyArray<Method>()
        val EMPTY_CLASS_ARRAY = emptyArray<Class<*>>()
        val EMPTY_FIELD_ARRAY = emptyArray<Field>()

        // 某给类定义的方法/字段缓存，k为要获取的类，v为该类所定义的方法列表
        @JvmField
        val declaredMethodsCache = HashMap<Class<*>, Array<Method>>()
        val declaredFieldsCache = HashMap<Class<*>, Array<Field>>()

        /**
         * 让一个字段变得可以访问
         * @param field 目标字段
         */
        @JvmStatic
        @Suppress("DEPRECATION", "JDK9中isAccessiable变为deprecated")
        fun makeAccessiable(field: Field) {
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
         * @param method 目标方法
         */
        @JvmStatic
        @Suppress("DEPRECATION", "JDK9中isAccessiable变为deprecated")
        fun makeAccessiable(method: Method) {
            // 1.如果方法不是public的
            // 2.如果类不是public的
            // 那么需要将该方法的可见性改为true
            if ((!Modifier.isPublic(method.modifiers) || !Modifier.isPublic(method.declaringClass.modifiers)) && !method.isAccessible) {
                method.isAccessible = true
            }
        }

        /**
         * 让一个构造器变得可以访问
         * @param constructor 目标构造器
         */
        @JvmStatic
        @Suppress("DEPRECATION", "JDK9中isAccessiable变为deprecated")
        fun makeAccessiable(constructor: Constructor<*>) {
            // 1.如果构造器不是public的
            // 2.如果类不是public的
            // 那么需要将该构造器的可见性改为true
            if ((!Modifier.isPublic(constructor.modifiers) || !Modifier.isPublic(constructor.declaringClass.modifiers)) && !constructor.isAccessible) {
                constructor.isAccessible = true
            }
        }


        /**
         * 在一个类上name和type都匹配的字段，如果没找到，return null
         * @param clazz 目标类
         * @param name 字段名
         */
        @JvmStatic
        fun findField(clazz: Class<*>, name: String): Field? {
            return findField(clazz, name, null)
        }

        /**
         * 在一个类上name和type都匹配的字段，如果没找到，return null
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
                    // 如果typename匹配的话，return
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
         * @param filter 哪些方法需要进行操作？
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
            declaredFields.filter(filter).forEach { action.invoke(it) }
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
         * 获取一个类定义的所有字段
         * @param clazz 目标类
         * @param defensive 是否具有侵入性的，如果defensive=true，那么需要克隆一份进行返回
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
         * @param field 字段
         * @param target 要获取的字段的目标对象
         */
        @JvmStatic
        fun getField(field: Field, target: Any): Any? {
            return field[target]
        }

        /**
         * 设置某个字段的值
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
         * @param clazz 目标类
         * @param name 方法name
         */
        @JvmStatic
        fun findMethod(clazz: Class<*>, name: String): Method? {
            return findMethod(clazz, name, *EMPTY_CLASS_ARRAY)
        }

        /**
         * 根据name和参数类型列表去指定类当中去寻找方法，有可能没找到，return null
         * @param clazz 目标类
         * @param name 方法name
         * @param parameterTypes 方法的参数类型
         */
        @JvmStatic
        fun findMethod(clazz: Class<*>, name: String, vararg parameterTypes: Class<*>): Method? {
            return clazz.getDeclaredMethod(name, *parameterTypes)
        }

        /**
         * 反射执行目标方法
         * @param method 目标方法
         * @param target 目标方法要传递的this对象
         */
        @JvmStatic
        fun invokeMethod(method: Method, target: Any?): Any? {
            return method.invoke(target)
        }

        /**
         * 反射执行目标方法
         * @param method 目标方法
         * @param target 目标方法要传递的this对象
         * @param args 执行目标方法需要的参数列表，在拿到参数之后，会是以数组的方式去进行获取，但是method.invoke时
         * 又是传递的vararg，就导致了数组里套数组的情况，需要使用*将数组中的元素继续进行拆分开
         * @return 执行目标方法返回的对象
         */
        @JvmStatic
        fun invokeMethod(method: Method, target: Any?, vararg args: Any?): Any? {
            return method.invoke(target, *args)
        }

        /**
         * 判断一个方法是否是equals方法
         * @param method 要进行判断的方法
         */
        @JvmStatic
        fun isEqualsMethod(method: Method): Boolean {
            // 判断方法名和参数数量和参数类型
            return method.name == "equals" && method.parameterCount == 1 && method.parameterTypes[0] == Any::class.java
        }

        /**
         * 判断一个方法是否是toString方法
         * @param method 要进行判断的方法
         */
        @JvmStatic
        fun isToStringMethod(method: Method): Boolean {
            return method.name == "toString" && method.parameterCount == 0
        }

        /**
         * 判断一个方法是否是hashCode方法
         * @param method 要进行判断的方法
         */
        @JvmStatic
        fun isHashCodeMethod(method: Method): Boolean {
            return method.name == "hashCode" && method.parameterCount == 0
        }

        /**
         * 判断这个方法是否来自于Object类，可以重写的equals/hashCode/toString方法也需要进行判断
         * @param method 要进行判断的方法
         */
        @JvmStatic
        fun isObjectMethod(method: Method): Boolean {
            return method.declaringClass == Any::class.java || isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(
                method
            )
        }

        /**
         * 对一个类当中定义的所有方法，执行同样的操作
         * @param clazz 要执行方法的类
         * @param action 要执行的操作
         */
        @JvmStatic
        fun doWithLocalMethods(clazz: Class<*>, action: (Method) -> Unit) {
            doWithMethods(clazz, action) { true }
        }

        /**
         * 对一个类当中定义的所有方法，执行同样的操作
         * @param clazz 要执行方法的类
         * @param action 要执行的操作
         * @param filter 该方法是否要执行的Filter？return true->执行，else->不执行
         */
        @JvmStatic
        fun doWithLocalMethods(clazz: Class<*>, action: (Method) -> Unit, filter: (Method) -> Boolean) {
            val declaredMethods = getDeclaredMethods(clazz, false)
            declaredMethods.filter(filter).forEach { action.invoke(it) }
        }

        /**
         * 对一个类以及它父类当中定义的所有方法，执行同样的操作
         * @param clazz 要执行方法的类
         * @param action 要执行的操作
         */
        @JvmStatic
        fun doWithMethods(clazz: Class<*>, action: (Method) -> Unit) {
            doWithMethods(clazz, action) { true }
        }

        /**
         * 对一个类以及它父类当中定义的所有方法，执行同样的操作
         * @param clazz 要执行方法的类
         * @param action 要执行的操作
         * @param filter 该方法是否要执行的Filter？return true->执行，else->不执行
         */
        @JvmStatic
        fun doWithMethods(clazz: Class<*>, action: (Method) -> Unit, filter: (Method) -> Boolean) {
            val declaredMethods = getDeclaredMethods(clazz, false)
            declaredMethods.filter(filter).forEach { action.invoke(it) }

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
         */
        @JvmStatic
        fun getAllDeclaredMethods(clazz: Class<*>): Array<Method> {
            val allDeclaredMethods = ArrayList<Method>()
            // 因为doWithMethods，正好会遍历所有的方法，正好利用该方法...
            doWithMethods(clazz, allDeclaredMethods::add)
            return allDeclaredMethods.toArray(emptyArray<Method>())
        }

        /**
         * 获取一个类定义的所有方法，因为有可能具有侵入性，会改变缓存中的declaredMethod数组，因此clone一份出来return，
         * 通过defensive去进行实现
         * @param clazz 要获取方法的类
         */
        @JvmStatic
        fun getDeclaredMethods(clazz: Class<*>): Array<Method> {
            return getDeclaredMethods(clazz, true)
        }

        /**
         * 获取一个类定义的所有方法
         * @param clazz 要获取方法的类
         * @param defensive 这个方法是否具有侵入性？也就是需不需要将数据clone一份出来返回？true代表需要，反之不需要
         */
        @JvmStatic
        fun getDeclaredMethods(clazz: Class<*>, defensive: Boolean): Array<Method> {
            // 首先尝试，从缓存当中获取该类定义的方法，获取不到，那么去进行构建...
            var result = declaredMethodsCache[clazz]
            if (result == null) {
                val declaredMethods: Array<Method> = clazz.declaredMethods
                val defaultMethods: List<Method> = findConcreteMethodsOnInterfaces(clazz)
                // 将declaredMethod列表和defaultMethod列表全部拷贝到result当中，并放入到缓存当中(如果为空的话，需要放入一个空方法的常量值)
                // Array，第二个参数是传递的数组的index->Array[index]的元素产生方法
                result = Array(declaredMethods.size + defaultMethods.size) { index ->
                    if (index < declaredMethods.size) declaredMethods[index] else defaultMethods[index]
                }
                declaredMethodsCache[clazz] = if (result.isEmpty()) EMPTY_METHOD_ARRAY else result
            }
            // 如果defensive=true，需要clone一份进行返回，如果defensive=false，那么直接返回元对象
            return if (defensive) result.clone() else result
        }

        /**
         * 在一个类的所有接口上去上寻找Concrete(具体的，已经进行实现的)方法，也就是default方法
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
}