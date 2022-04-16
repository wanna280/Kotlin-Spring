package com.wanna.framework.context

interface ConfigurableBeanFactory : BeanFactory {

    /**
     * 获取BeanFactory的ClassLoader
     */
    fun getBeanClassLoader(): ClassLoader

    /**
     * 类型转换器，用在Spring当中完成类型的转换
     */
    fun getTypeConverter(): TypeConverter
    fun setTypeConverter(typeConverter: TypeConverter)

    /**
     * 添加字符串的值解析器，比如用来解析"${}"占位符表达式
     */
    fun addEmbeddedValueResolver(resolver: StringValueResolver)

    /**
     * 是否有嵌入式的值解析器
     */
    fun hasEmbeddedValueResolver(): Boolean

    /**
     * 解析表达式
     */
    fun resolveEmbeddedValue(strVal: String?): String?

    /**
     * 设置BeanClassLoader
     * @param classLoader 要设置的ClassLoader，如果为空，将会使用默认的ClassLoader
     */
    fun setBeanClassLoader(classLoader: ClassLoader?)
}