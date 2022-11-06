package com.wanna.framework.test.context.support

import com.wanna.framework.context.annotation.AnnotatedBeanDefinitionReader
import com.wanna.framework.context.annotation.BeanDefinitionReader
import com.wanna.framework.context.support.GenericApplicationContext
import com.wanna.framework.test.context.ContextLoader
import com.wanna.framework.test.context.MergedContextConfiguration

/**
 * 基于注解的配置的[ContextLoader]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
open class AnnotationConfigContextLoader : AbstractGenericContextLoader() {

    override fun loadBeanDefinitions(
        context: GenericApplicationContext,
        mergedContextConfiguration: MergedContextConfiguration
    ) {
        // 注册配置类
        AnnotatedBeanDefinitionReader(context).registerBean(*mergedContextConfiguration.getClasses())
    }


    override fun createBeanDefinitionReader(context: GenericApplicationContext): BeanDefinitionReader {
        TODO("Not yet implemented")
    }
}