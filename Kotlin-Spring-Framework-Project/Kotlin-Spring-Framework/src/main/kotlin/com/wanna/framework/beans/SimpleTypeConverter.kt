package com.wanna.framework.beans


/**
 * 这是一个简单的TypeConverter的实现
 */
class SimpleTypeConverter : TypeConverterSupport() {

    init {
        this.delegate = TypeConverterDelegate(this)
    }
}