package com.wanna.framework.scheduling.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Role
import com.wanna.framework.scheduling.config.TaskManagementConfigUtils

@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration(proxyBeanMethods = false)
open class SchedulingConfiguration {

    @Bean(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    open fun scheduledAnnotationBeanPostProcessor(): ScheduledAnnotationBeanPostProcessor {
        return ScheduledAnnotationBeanPostProcessor()
    }
}