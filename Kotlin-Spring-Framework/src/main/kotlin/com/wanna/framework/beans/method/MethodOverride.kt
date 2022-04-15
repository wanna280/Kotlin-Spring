package com.wanna.framework.beans.method

import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataElement
import javax.xml.transform.Source

/**
 * 它描述了Spring当中的运行时方法重写，在注解版IOC容器中使用@Lookup或者
 * 在XML版本中使用lookup-method或者replace-method两种方式可以去实现运行时方法重写
 */
open class MethodOverride(val methodName: String) : BeanMetadataElement {

    private var source: Any? = null

    override fun getSource(): Any? {
        return source
    }

    fun setSource(source: Any?) {
        this.source = source
    }
}