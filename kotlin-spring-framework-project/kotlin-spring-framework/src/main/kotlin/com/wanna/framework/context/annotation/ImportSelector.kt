package com.wanna.framework.context.annotation

import com.wanna.framework.core.type.AnnotationMetadata
import java.util.function.Predicate

/**
 * ImportSelector，可以使用编码的方式给SpringBeanFactory当中去批量导入组件；
 * 当然，类似地：你也可以使用@Import注解给SpringBeanFactory当中批量导入组件，
 * 也可以使用ImportBeanDefinitionRegistrar去进行手动完成组件的导入
 *
 * ## Note
 * ImportSelector导入的所有配置类，都当做配置类去进行解析，也就是支持去解析该配置类上的所有注解
 *
 * @see Import
 * @see ImportBeanDefinitionRegistrar
 */
fun interface ImportSelector {
    /**
     * 设置排除的Filter，符合Filter规范的组件，将会被排除掉
     *
     * @return 要使用的排除的Filter
     */
    fun getExclusionFilter(): Predicate<String>? {
        return null
    }

    /**
     * 给容器当中导入组件
     *
     * @param metadata 导入ImportSelector的注解信息
     * @return 要给容器中导入组件的className列表
     */
    fun selectImports(metadata: AnnotationMetadata): Array<String>
}