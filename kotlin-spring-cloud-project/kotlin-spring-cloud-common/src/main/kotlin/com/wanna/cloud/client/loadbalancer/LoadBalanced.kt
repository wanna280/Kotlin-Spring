package com.wanna.cloud.client.loadbalancer

import com.wanna.framework.beans.factory.annotation.Qualifier

/**
 * 标识这是一个支持LoadBalance的客户端, 仅仅是标识作用, 主要作用是加一个Qualifier注解, 保证注入方和Bean能够去进行匹配
 *
 * @see Qualifier
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Qualifier
annotation class LoadBalanced
