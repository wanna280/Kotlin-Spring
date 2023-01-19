package com.wanna.framework.core.annotation

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.core.annotation.MergedAnnotations.SearchStrategy
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.common.logging.LoggerFactory
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

/**
 * 注解相关的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/1
 */
object AnnotationUtils {

    /**
     * Logger
     */
    @JvmStatic
    private val logger = LoggerFactory.getLogger(AnnotationUtils::class.java)


    /**
     * 获取给定的注解实例的AnnotationAttributes
     *
     * @param  annotation Annotation
     * @return AnnotationAttributes
     */
    @JvmStatic
    fun getAnnotationAttributes(annotation: Annotation): AnnotationAttributes =
        getAnnotationAttributes(null, annotation)

    /**
     * 获取给定的注解实例的AnnotationAttributes
     *
     * @param source source
     * @param  annotation Annotation
     * @return AnnotationAttributes
     */
    @JvmStatic
    fun getAnnotationAttributes(@Nullable source: AnnotatedElement?, annotation: Annotation): AnnotationAttributes =
        getAnnotationAttributes(source, annotation, false, false)

    /**
     * 获取给定的注解实例的AnnotationAttributes
     *
     * @param  annotation Annotation
     * @param classValueAsString 是否需要将Class转为String去进行收集
     * @param nestedAnnotationsAsMap 是否需要将内部的注解转换为Map去进行收集
     * @return AnnotationAttributes
     */
    @JvmStatic
    fun getAnnotationAttributes(
        annotation: Annotation, classValueAsString: Boolean, nestedAnnotationsAsMap: Boolean
    ): AnnotationAttributes = getAnnotationAttributes(null, annotation, classValueAsString, nestedAnnotationsAsMap)

    /**
     * 获取给定的注解实例的AnnotationAttributes
     *
     * @param source source
     * @param  annotation Annotation
     * @param classValueAsString 是否需要将Class转为String去进行收集
     * @param nestedAnnotationsAsMap 是否需要将内部的注解转换为Map去进行收集
     * @return AnnotationAttributes
     */
    @JvmStatic
    fun getAnnotationAttributes(
        @Nullable source: AnnotatedElement?,
        annotation: Annotation,
        classValueAsString: Boolean,
        nestedAnnotationsAsMap: Boolean
    ): AnnotationAttributes {
        val adapts = MergedAnnotation.Adapt.values(classValueAsString, nestedAnnotationsAsMap)
        return MergedAnnotation
            .from(source, annotation)
            .withNonMergedAttributes()  // with non merge
            .asAnnotationAttributes(*adapts)  // to AnnotationAttributes
    }

    /**
     * 从给定的方法上去寻找到第一个符合给定注解类型的注解对象
     * (Note: 只去检查直接标注/一级Meta注解的标注方式, 并且将不会使用MergedValue, 也就是`@AliasFor`注解不会生效)
     *
     * @param method 要去进行寻找注解的目标方法
     * @param annotationType 要去进行寻找的目标注解类型
     * @param A 需要的注解类型
     * @return 找到的注解类型匹配的第一个注解对象(如果没有找到合适的注解的话, return null)
     */
    @Nullable
    @JvmStatic
    fun <A : Annotation> getAnnotation(method: Method, annotationType: Class<A>): A? {
        return getAnnotation((method as AnnotatedElement), annotationType)
    }

    /**
     * 从给定的AnnotatedElement上去寻找到第一个符合给定注解类型的注解对象
     * (Note: 只去检查直接标注/一级Meta注解的标注方式, 并且将不会使用MergedValue, 也就是`@AliasFor`注解不会生效)
     *
     * @param element 要去进行寻找的目标元素(字段/方法/类/构造器等)
     * @param annotationType 要去进行寻找的目标注解类型
     * @param A 需要的注解类型
     * @return 找到的注解类型匹配的第一个注解对象(如果没有找到合适的注解的话, return null)
     */
    @Nullable
    @JvmStatic
    fun <A : Annotation> getAnnotation(element: AnnotatedElement, annotationType: Class<A>): A? {
        // 对于一些简单的, 可能只有简单的Java注解的方法, 那么直接在该方法上去进行寻找
        if (AnnotationFilter.PLAIN.matches(annotationType) || AnnotationsScanner.hasPlainJavaAnnotationsOnly(element)) {
            return element.getAnnotation(annotationType)
        }

        // 对于非简单的情况, 我们去执行Merge, 得到MergedAnnotation...
        return MergedAnnotations
            .from(element, SearchStrategy.INHERITED_ANNOTATIONS, RepeatableContainers.none())
            .get(annotationType)  // getAnnotation
            .withNonMergedAttributes()   // non merged values
            .synthesize(AnnotationUtils::isSingleLevelPresent) // check single level
            .orElse(null)
    }

    /**
     * 从给定的方法上去寻找到第一个符合给定注解类型的注解对象
     * (Note: 支持去进行多级的Meta注解的寻找, 支持在继承的类的方法身上去进行寻找Meta注解, 但是也不会使用MergedValue, 也就是`@AliasFor`注解不会生效)
     *
     * @param method 要去进行寻找注解的目标方法
     * @param annotationType 要去进行寻找的目标注解类型
     * @param A 需要的注解类型
     * @return 找到的注解类型匹配的第一个注解对象(如果没有找到合适的注解的话, return null)
     */
    @Nullable
    @JvmStatic
    fun <A : Annotation> findAnnotation(method: Method, annotationType: Class<A>?): A? {
        annotationType ?: return null
        // 对于一些简单的, 可能只有简单的Java注解的方法, 那么直接在该方法上去进行寻找
        if (AnnotationFilter.PLAIN.matches(annotationType) || AnnotationsScanner.hasPlainJavaAnnotationsOnly(method)) {
            return method.getDeclaredAnnotation(annotationType)
        }

        // 对于非简单的情况, 我们去执行Merge, 得到MergedAnnotation...
        return MergedAnnotations
            .from(method, SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none()) // TypeHierarchy Search
            .get(annotationType)  // getAnnotation
            .withNonMergedAttributes()  // non merged values
            .synthesize(MergedAnnotation<A>::present)  // synthesize if present
            .orElse(null)
    }

    /**
     * 从给定的类上去寻找到第一个符合给定注解类型的注解对象
     * (Note: 支持去进行多级的Meta注解的寻找, 支持在继承的类的身上去进行寻找Meta注解, 但是也不会使用MergedValue, 也就是`@AliasFor`注解不会生效)
     *
     * @param clazz 要去进行寻找注解的目标类
     * @param annotationType 要去进行寻找的目标注解类型
     * @param A 需要的注解类型
     * @return 找到的注解类型匹配的第一个注解对象(如果没有找到合适的注解的话, return null)
     */
    @Nullable
    @JvmStatic
    fun <A : Annotation> findAnnotation(clazz: Class<*>, annotationType: Class<A>?): A? {
        annotationType ?: return null

        // 对于一些只存在有简单注Java注解的情况, 我们可以在类身上去寻找, 无需去进行merge
        if (AnnotationFilter.PLAIN.matches(annotationType) || AnnotationsScanner.hasPlainJavaAnnotationsOnly(clazz)) {
            val annotation = clazz.getDeclaredAnnotation(annotationType)
            if (annotation != null) {
                return annotation
            }

            // 我们尝试superClass去进行寻找, 就算没有标注@Inherited注解的, 我们也支持去进行处理, 我们直接到父类身上去寻找
            // 例如要去搜索一个类身上是否有@Deprecated注解的情况...
            val superclass = clazz.superclass
            if (superclass == null || superclass == Any::class.java) {
                return null
            }

            // 让superClass去进行递归寻找...
            return findAnnotation(superclass, annotationType)
        }

        // 对于非简单的情况, 我们去执行Merge, 得到MergedAnnotation...
        return MergedAnnotations
            .from(clazz, SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none()) // TypeHierarchy Search
            .get(annotationType)  // getAnnotation
            .withNonMergedAttributes()  // non merged values
            .synthesize(MergedAnnotation<A>::present)  // synthesize if present
            .orElse(null)
    }

    /**
     * 从给定的AnnotatedElement上去寻找到第一个符合给定注解类型的注解对象
     * (Note: 支持去进行多级的Meta注解的寻找, 但是也不会使用MergedValue, 也就是`@AliasFor`注解不会生效)
     *
     * @param element 要去进行寻找的目标元素(字段/方法/类/构造器等)
     * @param annotationType 要去进行寻找的目标注解类型
     * @param A 需要的注解类型
     * @return 找到的注解类型匹配的第一个注解对象(如果没有找到合适的注解的话, return null)
     */
    @Nullable
    @JvmStatic
    fun <A : Annotation> findAnnotation(element: AnnotatedElement, @Nullable annotationType: Class<A>?): A? {
        annotationType ?: return null
        // 检查是否只有简单的Java注解, 如果只有简单的Java注解的话, 那么我们就不走merge逻辑了
        if (AnnotationFilter.PLAIN.matches(annotationType) || AnnotationsScanner.hasPlainJavaAnnotationsOnly(element)) {
            return element.getDeclaredAnnotation(annotationType)
        }

        // 对于非简单的情况, 我们去执行Merge, 得到MergedAnnotation...
        return MergedAnnotations
            .from(element, SearchStrategy.INHERITED_ANNOTATIONS, RepeatableContainers.none())
            .get(annotationType)  // getAnnotation
            .withNonMergedAttributes()  // non merged values
            .synthesize(MergedAnnotation<A>::present)  // synthesize if present
            .orElse(null)
    }

    /**
     * 检查给定的MergedAnnotation的distance, 检查该注解是否是单Level的标注?
     * 也就是检查该注解是否直接标注, 或者以一级Meta注解的方式去进行标注?
     *
     * @param mergedAnnotation MergedAnnotation
     * @return distance为0/1, return true; 否则return false
     */
    @JvmStatic
    private fun <A : Annotation> isSingleLevelPresent(mergedAnnotation: MergedAnnotation<A>): Boolean {
        return mergedAnnotation.distance == 0 || mergedAnnotation.distance == 1
    }


    @JvmStatic
    fun handleIntrospectionFailure(@Nullable element: AnnotatedElement?, ex: Throwable) {
        rethrowAnnotationConfigurationException(ex)

        var meta = false
        if (element is Class<*> && ClassUtils.isAssignFrom(Annotation::class.java, element)) {
            meta = true
        }
        if (logger.isInfoEnabled) {
            if (meta) {
                logger.info("Failed to meta-introspect annotation $element : $ex")
            } else {
                logger.info("Failed to introspect annotations on $element : $ex")
            }
        }
    }

    @JvmStatic
    private fun rethrowAnnotationConfigurationException(ex: Throwable) {
        if (ex is AnnotationConfigurationException) {
            throw ex
        }
    }

    /**
     * 清除AnnotationUtils相关的缓存
     */
    @JvmStatic
    fun clearCache() {
        AnnotationTypeMappings.clearCache()
        AnnotationsScanner.clearCache()
    }
}