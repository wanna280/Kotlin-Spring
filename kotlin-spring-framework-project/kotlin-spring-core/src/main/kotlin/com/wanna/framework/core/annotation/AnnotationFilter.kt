package com.wanna.framework.core.annotation

import javax.annotation.Nullable

/**
 * 提供对于注解的匹配的Filter, 如果该注解和AnnotationFilter匹配了, 那么说明该注解需要被过滤掉
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
fun interface AnnotationFilter {
    /**
     * 执行对于目标注解对象的匹配
     *
     * @param type 注解对象
     * @return 如果该注解的类名和AnnotationFilter匹配的话, 那么return true; 否则return false
     */
    fun matches(@Nullable type: Annotation?): Boolean = type != null && matches(type.annotationClass.java)


    /**
     * 执行对于目标注解类型的匹配
     *
     * @param type 注解类型Class
     * @return 如果该注解的类名和AnnotationFilter匹配的话, 那么return true; 否则return false
     */
    fun matches(@Nullable type: Class<*>?): Boolean = type != null && matches(type.name)

    /**
     * 执行对于目标注解类型(className)的匹配
     *
     * @param typeName 注解的ClassName
     * @return 如果typeName和AnnotationFilter匹配的话, 那么return true; 否则return false
     */
    fun matches(typeName: String?): Boolean

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
            override fun matches(@Nullable type: Annotation?): Boolean = true
            override fun matches(@Nullable type: Class<*>?): Boolean = true
            override fun matches(@Nullable typeName: String?): Boolean = true
            override fun toString(): String = "ALL Filtered"
        }

        /**
         * 基于packages去对注解去进行过滤的Filter
         *
         * @param packages 要去进行过滤的包
         * @return 提供对于指定的包去进行过滤的AnnotationFilter
         */
        @JvmStatic
        fun packages(vararg packages: String): AnnotationFilter = PackagesAnnotationFilter(packages = packages)
    }
}