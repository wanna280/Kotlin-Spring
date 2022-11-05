package com.wanna.framework.test.context.support

import com.wanna.framework.test.context.ContextLoader
import com.wanna.framework.test.context.SmartContextLoader

/**
 * Delegating的[SmartContextLoader]，通过委托别的[ContextLoader]去实现[SmartContextLoader]的功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
open class DelegatingSmartContextLoader : AbstractDelegatingSmartContextLoader() {

    private val annotationLoader = AnnotationConfigContextLoader()

    override fun getAnnotationConfigLoader(): SmartContextLoader = annotationLoader

    override fun getXmlLoader(): SmartContextLoader = annotationLoader
}