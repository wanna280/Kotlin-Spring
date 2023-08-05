package com.wanna.boot.test.context

import com.wanna.framework.context.annotation.ClassPathScanningCandidateComponentProvider
import com.wanna.framework.core.type.filter.AnnotationTypeFilter
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import java.util.*

/**
 * 标注了某个注解的类的寻找器, 支持从某个包下去找到标注了某个注解的类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 *
 * @param annotationType 要去寻找的注解类型(例如@SpringBootConfiguration注解)
 */
class AnnotatedClassFinder(private val annotationType: Class<out Annotation>) {

    companion object {
        /**
         * Cache
         */
        @JvmStatic
        private val cache = Collections.synchronizedMap(LinkedHashMap<String, Class<*>?>())
    }


    /**
     * ClassPath的候选组件的扫描器
     */
    private val scanner = ClassPathScanningCandidateComponentProvider(false)

    init {
        // 添加一个指定的注解的IncludeFilter
        scanner.addIncludeFilter(AnnotationTypeFilter(annotationType))
    }

    /**
     * 根据给定的类所在的包去扫描到对应标注了给定的注解的类
     *
     * @param source 要扫描的包下的一个类
     * @return 扫描到的标注了给定的注解到了类(没有找到return null)
     */
    @Nullable
    fun findFromClass(source: Class<*>): Class<*>? {
        return findFromPackage(ClassUtils.getPackageName(source.name))
    }

    /**
     * 根据给定的包去扫描到对应标注了给定的注解的类
     *
     * @param source 待扫描的包
     * @return 扫描到的标注了给定的注解到了类(没有找到return null)
     */
    @Nullable
    fun findFromPackage(source: String): Class<*>? {
        var clazz = cache[source]
        if (clazz == null) {
            clazz = scanPackage(source)
            cache[source] = clazz
        }
        return clazz
    }

    /**
     * 从给定的包以及其所有的父包当中去寻找合适的类
     *
     * @param source 需要去进行搜寻的包
     * @return 扫描到的标注了给定的注解的类(没有找到的话, return null)
     */
    @Nullable
    private fun scanPackage(source: String): Class<*>? {
        var candidate = source
        while (candidate.isNotEmpty()) {
            val components = this.scanner.scanCandidateComponents(candidate)
            if (components.isNotEmpty()) {
                if (components.size > 1) {
                    throw IllegalStateException("在给定的[$source]包下去扫描到了多个标注了@[${annotationType.name}]的类")
                }
                return ClassUtils.resolveClassName(components.iterator().next().getBeanClassName()!!, null)
            }

            // 尝试从父包当中去进行寻找
            candidate = getParentPackage(candidate)
        }
        return null
    }

    /**
     * 获取一个包对应的父包
     *
     * @param sourcePackage sourcePackage
     * @return parentPackage(sourcePackage去掉末尾的一个"."之后的字符串)
     */
    private fun getParentPackage(sourcePackage: String): String {
        val lastDot = sourcePackage.lastIndexOf('.')
        return if (lastDot != -1) sourcePackage.substring(0, lastDot) else ""
    }
}