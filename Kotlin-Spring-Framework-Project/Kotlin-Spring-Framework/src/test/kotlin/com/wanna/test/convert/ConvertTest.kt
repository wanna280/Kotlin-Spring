package com.wanna.test.convert

import com.wanna.framework.beans.SimpleTypeConverter
import com.wanna.framework.constants.CLASS_ARRAY_TYPE
import com.wanna.framework.core.convert.support.CollectionToStringConverter
import com.wanna.framework.core.convert.support.DefaultConversionService
import java.nio.charset.Charset
import java.util.UUID

fun main() {
    val typeConverter = SimpleTypeConverter()
    typeConverter.setConversionService(DefaultConversionService())

    println(typeConverter.convertIfNecessary("234", Int::class.java))

    println(typeConverter.convertIfNecessary("SimpleTypeConverter", Class::class.java))

    println(typeConverter.convertIfNecessary("utf-8", Charset::class.java))

    val classArrayString = "SimpleTypeConverter,SimpleTypeConverter"
    println(typeConverter.convertIfNecessary(classArrayString, CLASS_ARRAY_TYPE)!!.contentToString())

    println(typeConverter.convertIfNecessary("de2e6c4a-0809-4fbe-97a6-f0f785b87893", UUID::class.java))

    val set = setOf("wanna", "123")
    val converted = CollectionToStringConverter().convert(set, Set::class.java, String::class.java)
    println(converted)


}