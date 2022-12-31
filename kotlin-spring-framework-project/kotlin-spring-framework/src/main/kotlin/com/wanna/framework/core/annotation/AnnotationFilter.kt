package com.wanna.framework.core.annotation

/**
 * 提供对于注解的匹配的Filter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
fun interface AnnotationFilter {

    fun matches(type: Annotation): Boolean = matches(type.annotationClass.java)

    fun matches(type: Class<*>): Boolean = matches(type.name)

    fun matches(typeName: String): Boolean

    companion object {

        /**
         * 匹配"java.lang"/"com.wanna.framework.lang"包下的注解
         */
        @JvmStatic
        val PLAIN = packages("java.lang", "com.wanna.framework.lang", "kotlin")

        /**
         * 匹配"java"和"javax"包下的注解
         */
        @JvmStatic
        val JAVA = packages("java", "javax")

        /**
         * 匹配所有注解的AnnotationFilter
         */
        @JvmStatic
        val ALL = object : AnnotationFilter {
            override fun matches(type: Annotation): Boolean = true
            override fun matches(type: Class<*>): Boolean = true
            override fun matches(typeName: String): Boolean = true
        }

        @JvmStatic
        fun packages(vararg packages: String): AnnotationFilter = PackagesAnnotationFilter(packages = packages)
    }
}