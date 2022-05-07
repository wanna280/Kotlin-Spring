package com.wanna.framework.context.annotation

/**
 * 标注这个注解的Bean，可以去进行懒加载，在运行时再去对Bean完成实例化和初始化，在容器启动时并不去完成
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Lazy()
