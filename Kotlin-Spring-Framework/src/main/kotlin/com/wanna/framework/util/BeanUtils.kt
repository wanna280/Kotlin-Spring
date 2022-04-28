package com.wanna.framework.util

import java.lang.reflect.Constructor

class BeanUtils {
    companion object {
        /**
         * 通过有参数构造器去创建对象
         * @param ctor 构造器
         * @param args 参数列表
         */
        @JvmStatic
        fun <T> instantiateClass(ctor: Constructor<T>, vararg args: Any?): T {
            return ctor.newInstance(*args)
        }

        /**
         * 通过无参数构造器去创建对象
         * @param ctor 无参构造器
         */
        @JvmStatic
        fun <T> instantiateClass(ctor: Constructor<T>): T {
            return ctor.newInstance()
        }

        /**
         * 通过无参数构造器创建对象
         */
        @JvmStatic
        fun <T> instantiateClass(clazz: Class<T>): T {
            return clazz.getDeclaredConstructor().newInstance()
        }
    }
}