package com.wanna.framework.context.annotation

import com.wanna.framework.context.util.ConfigurationClass
import java.lang.reflect.Method

/**
 * 标识这是一个BeanMethod，也就是被@Bean标注的方法
 */
open class BeanMethod(_method: Method,val configClass: ConfigurationClass) : ConfigurationMethod(_method) {

}