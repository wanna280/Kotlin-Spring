package com.wanna.framework.scheduling.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Role
import com.wanna.framework.scheduling.config.TaskManagementConfigUtils

/**
 * 为定时调度提供支持的配置类
 *
 * @see ScheduledAnnotationBeanPostProcessor
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration(proxyBeanMethods = false)
open class SchedulingConfiguration {

    /**
     * 为Spring BeanFactory当中去导入一个处理[Scheduled]注解的BeanPostProcessor
     *
     * @return ScheduledAnnotationBeanPostProcessor
     */
    @Bean(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    open fun scheduledAnnotationBeanPostProcessor(): ScheduledAnnotationBeanPostProcessor {
        return ScheduledAnnotationBeanPostProcessor()
    }
}