package com.wanna.framework.context.weaving

import com.wanna.framework.instrument.classloading.LoadTimeWeaver
import java.lang.instrument.ClassFileTransformer

/**
 * 这是一个默认的LoadTimeWeaver的实现
 */
open class DefaultContextLoadTimeWeaver(private val classLoader: ClassLoader) : LoadTimeWeaver {
    override fun addTransformer(transformer: ClassFileTransformer) {

    }

    override fun getInstrumentableClassLoader(): ClassLoader {
        return this.classLoader
    }

    override fun getThrowawayClassLoader(): ClassLoader {
        return null!!
    }
}