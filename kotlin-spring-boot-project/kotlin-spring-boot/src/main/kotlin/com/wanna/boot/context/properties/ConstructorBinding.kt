package com.wanna.boot.context.properties

/**
 * 标注这个注解的, 支持使用构造器去的方式去进行绑定, 而不是使用JavaBean的setter的方式去进行绑定, 这个注解可以标注在类型上/构造器上;
 * (1)如果是类型上, 那么Kotlin类型只能匹配主构造器, Java类型只能匹配一个有参数构造器(有多个构造器/只要一个无参数构造器, 都不能匹配)
 * (2)如果是在构造器上, 那么将会匹配所有的构造器, 去找到一个合适的构造器; 标注了ConstructorBinding的无参数构造器即可匹配, 找到了多
 * 个ConstructorBinding或者是ConstructorBinding被标注在了无参数构造器上, 都会抛出不合法的状态异常
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
annotation class ConstructorBinding