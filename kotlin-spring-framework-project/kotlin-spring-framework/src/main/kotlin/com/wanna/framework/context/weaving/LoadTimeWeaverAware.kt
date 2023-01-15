package com.wanna.framework.context.weaving

import com.wanna.framework.context.aware.Aware
import com.wanna.framework.instrument.classloading.LoadTimeWeaver

/**
 * 这是一个完成加载时编制的对象的注入的Aware接口, 通过此接口, 可以注入容器当中的LoadTimeWeaver对象
 *
 * @see LoadTimeWeaver
 */
fun interface LoadTimeWeaverAware : Aware {

    /**
     * 注入LoadTimeWeaver
     *
     * @param loadTimeWeaver LoadTimeWeaver
     */
    fun setLoadTimeWeaver(loadTimeWeaver: LoadTimeWeaver)
}