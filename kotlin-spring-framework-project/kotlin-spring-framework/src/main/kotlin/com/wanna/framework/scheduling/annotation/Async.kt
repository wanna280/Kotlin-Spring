package com.wanna.framework.scheduling.annotation

/**
 * 标注@Async注解的方法将会被异步执行, 也可以标注@Async在类型上, 标识所有的方法都将被异步执行; 
 * 但是需要注意的是, 并不支持标注在@Configuration的类上; 
 *
 * 异步方法可以支持任何类型的方法参数, 但是方法的返回值只能是void或者是juc下的Future
 *
 * @see java.util.concurrent.Future
 * @see java.util.concurrent.Executor
 *
 * @param value 指定要使用哪个线程池去执行异步任务? 可以指定一个juc包下的Executor, 或者是Spring自家的TaskExecutor,
 * 如果该属性配置在方法层面上, 只要方法级别会使用该线程池; 如果该属性配置在类层面上, 该类当中的所有方法都会异步执行, 并应用该线程池; 
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class Async(val value: String = "")
