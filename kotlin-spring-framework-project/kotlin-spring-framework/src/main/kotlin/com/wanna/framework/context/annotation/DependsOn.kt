package com.wanna.framework.context.annotation

/**
 * 当前要往容器中加入的Bean, 需要依赖于哪些Bean才能完成创建, Spring在对当前Bean去进行创建之前, 会对所有的DependsOn的Bean去进行提前完成初始化和实例化;
 * 再去对当前的Bean去完成实例化和初始化工作, 可以标注在配置类和@Bean的方法上
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class DependsOn(val value: Array<String>)
