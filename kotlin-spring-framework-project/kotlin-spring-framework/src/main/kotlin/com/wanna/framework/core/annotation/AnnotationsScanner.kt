package com.wanna.framework.core.annotation

import com.wanna.framework.core.Ordered
import com.wanna.framework.core.annotation.MergedAnnotations.SearchStrategy.*
import com.wanna.framework.lang.Nullable
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
object AnnotationsScanner {

    /**
     * 空Method数组
     */
    @JvmStatic
    private val NO_METHODS = emptyArray<Method>()

    /**
     * 空Annotation数组
     */
    @JvmStatic
    private val NO_ANNOTATIONS = emptyArray<Annotation>()

    /**
     * 目标元素上的注解列表的缓存
     */
    @JvmStatic
    private val declaredAnnotationCache = ConcurrentHashMap<AnnotatedElement, Array<Annotation>>()

    @JvmStatic
    fun <C, R> scan(
        context: C,
        source: AnnotatedElement,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        val result = process(context, source, searchStrategy, processor)
        return processor.finish(result)
    }

    @JvmStatic
    private fun <C, R> process(
        context: C,
        source: AnnotatedElement,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return when (source) {
            is Class<*> -> processClass(context, source, searchStrategy, processor)
            is Method -> processMethod(context, source, searchStrategy, processor)
            else -> processElement(context, source, searchStrategy, processor)
        }
    }

    /**
     * 处理类
     */
    @JvmStatic
    private fun <C, R> processClass(
        context: C,
        source: Class<*>,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return when (searchStrategy) {
            DIRECT -> processElement(context, source, searchStrategy, processor)
            INHERITED_ANNOTATIONS -> processClassInheritedAnnotations(context, source, searchStrategy, processor)
            SUPERCLASS -> processClassHierarchy(context, source, processor, false, false)
            TYPE_HIERARCHY -> processClassHierarchy(context, source, processor, true, false)
            TYPE_HIERARCHY_AND_ENCLOSING_CLASSES -> processClassHierarchy(context, source, processor, true, true)
        }
    }

    /**
     * 处理方法
     */
    @JvmStatic
    private fun <C, R> processMethod(
        context: C,
        source: Method,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return when (searchStrategy) {
            DIRECT, INHERITED_ANNOTATIONS -> processMethodInheritedAnnotations(context, source, processor)

            // 只处理SuperClass的话includeInterfaces=false
            SUPERCLASS -> processMethodHierarchy(context, IntArray(1), source.declaringClass, processor, source, false)

            // 处理类型继承(TYPE_HIERARCHY)的话includeInterfaces=false
            TYPE_HIERARCHY, TYPE_HIERARCHY_AND_ENCLOSING_CLASSES -> processMethodHierarchy(
                context, IntArray(1), source.declaringClass, processor, source, true
            )
        }
    }

    /**
     * 带注解的继承关系地去处理一个类
     */
    @Nullable
    @JvmStatic
    private fun <C, R> processClassInheritedAnnotations(
        context: C,
        source: Class<*>,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return processElement(context, source, DIRECT, processor)
    }

    /**
     * 带继承关系地去处理一个类
     */
    @Nullable
    @JvmStatic
    private fun <C, R> processClassHierarchy(
        context: C,
        source: Class<*>,
        processor: AnnotationsProcessor<C, R>,
        includeInterfaces: Boolean,
        includeEnclosing: Boolean
    ): R? {
        return processElement(context, source, DIRECT, processor)
    }

    /**
     * 带继承关系地去处理一个方法
     *
     * @param context context(requiredType)
     * @param includeInterfaces 是否需要处理接口方法?
     */
    @Nullable
    private fun <C, R> processMethodHierarchy(
        context: C,
        aggregateIndex: IntArray,
        sourceClass: Class<*>,
        processor: AnnotationsProcessor<C, R>,
        rootMethod: Method,
        includeInterfaces: Boolean
    ): R? {

        try {
            var result = processor.doWithAggregate(context, aggregateIndex[0])
            if (result != null) {
                return result
            }
            if (hasPlainJavaAnnotationsOnly(sourceClass)) {
                return null
            }

            // 是否已经call了processor的标志位
            var calledProcessor = false

            // 如果sourceClass==rootMethod.declaringClass, 说明是第一次处理, 以目标方法优先的方式去进行醋栗
            if (sourceClass == rootMethod.declaringClass) {
                result = processMethodAnnotations(context, rootMethod, aggregateIndex[0], processor)
                calledProcessor = true
                if (result != null) {
                    return result
                }
            } else {
                for (method in getBaseTypeMethods(context, sourceClass)) {
                    result = processMethodAnnotations(context, method, aggregateIndex[0], processor)
                    calledProcessor = true
                    if (result != null) {
                        return result
                    }
                }
            }

            // 如果root方法是private, return null
            if (Modifier.isPrivate(rootMethod.modifiers)) {
                return null
            }
            if (calledProcessor) {
                aggregateIndex[0]++
            }

            // 如果需要处理接口的话, 那么针对sourceClass的所有的接口去进行递归处理
            if (includeInterfaces) {
                for (clazz in sourceClass.interfaces) {
                    val interfaceResult =
                        processMethodHierarchy(context, aggregateIndex, clazz, processor, rootMethod, includeInterfaces)
                    if (interfaceResult != null) {
                        return interfaceResult
                    }
                }
            }

            // 处理superClass
            val superclass = sourceClass.superclass
            if (superclass != null && superclass != Any::class.java) {
                val superClassResult = processMethodHierarchy(
                    context, aggregateIndex, sourceClass, processor, rootMethod, includeInterfaces
                )
                if (superClassResult != null) {
                    return superClassResult
                }
            }
        } catch (ex: Throwable) {
            AnnotationUtils.handleIntrospectionFailure(rootMethod, ex)
        }
        return null
    }

    /**
     * 处理方法继承的注解
     *
     * @param context context(requiredType)
     * @param source 要去进行处理的目标方法
     * @param processor AnnotationsProcessor
     */
    @Nullable
    private fun <C, R> processMethodInheritedAnnotations(
        context: C, source: Method, processor: AnnotationsProcessor<C, R>
    ): R? {
        try {
            val result = processor.doWithAggregate(context, 0)
            return result ?: processMethodAnnotations(context, source, 0, processor)
        } catch (ex: Throwable) {
            AnnotationUtils.handleIntrospectionFailure(source, ex)
        }
        return null
    }

    @Nullable
    private fun <C, R> processMethodAnnotations(
        context: C, source: Method, aggregateIndex: Int, processor: AnnotationsProcessor<C, R>
    ): R? {
        val annotations = getDeclaredAnnotations(source, false)
        val result = processor.doWithAnnotations(context, aggregateIndex, source, annotations)
        if (result != null) {
            return result
        }
        return null
    }

    /**
     * 处理一个AnnotatedElement上的注解
     */
    @Nullable
    @JvmStatic
    private fun <C, R> processElement(
        context: C,
        source: AnnotatedElement,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return processor.doWithAggregate(context, 0) ?: processor.doWithAnnotations(
            context, 0, source, getDeclaredAnnotations(source, false)
        )
    }

    /**
     * 获取到目标元素身上的所有的直接定义的注解(过滤掉了一些不需要的元注解)
     *
     * @param source source
     * @param defensive 是否具有侵入性? 如果具有侵入性的话, 那么需要clone一份去进行返回
     * @return declaredAnnotations
     */
    @JvmStatic
    fun getDeclaredAnnotations(source: AnnotatedElement, defensive: Boolean): Array<Annotation> {
        var cached = false
        var annotations = declaredAnnotationCache[source]
        if (annotations != null) {
            cached = true
        } else {
            annotations = source.declaredAnnotations
            var allIgnored = true
            if (annotations.isNotEmpty()) {
                val validAnnotations = ArrayList<Annotation>()
                annotations.forEach {
                    // 如果这个注解可以被忽略(比如一些元注解), 那么pass掉
                    if (isIgnorable(it.annotationClass.java)) {
                        return@forEach
                    }
                    allIgnored = false
                    validAnnotations.add(it)
                }
                // 如果全部注解都不是合法的注解的话, 那么我们直接去放入一个常量...
                annotations = if (allIgnored) NO_ANNOTATIONS else validAnnotations.toTypedArray()
                if (source is Class<*> || source is Member) {
                    declaredAnnotationCache[source] = annotations
                }
            }
        }

        if (!defensive || annotations!!.isEmpty() || !cached) {
            return annotations!!
        }
        return annotations.clone()
    }

    /**
     * 从给定的source上去找到AnnotationType类型的注解
     *
     * @param source 要去进行寻找注解的元素
     * @param annotationType 要去进行寻找的注解类型
     * @param A  要去进行寻找的注解类型
     * @return 从source上去找到的AnnotationType的注解(找不到return null)
     */
    @JvmStatic
    @Nullable
    @Suppress("UNCHECKED_CAST")
    fun <A : Annotation> getDeclaredAnnotation(source: AnnotatedElement, annotationType: Class<A>): A? {
        val declaredAnnotations = getDeclaredAnnotations(source, false)
        for (annotation in declaredAnnotations) {
            if (annotation.annotationClass.java == annotationType) {
                return annotation as A
            }
        }
        return null
    }

    @JvmStatic
    private fun <C> getBaseTypeMethods(context: C, baseType: Class<*>): Array<Method> {
        if (baseType == Any::class.java || hasPlainJavaAnnotationsOnly(baseType)) {
            return NO_METHODS
        }
        return emptyArray()
    }

    /**
     * 检查给定的类上是否只有一些简单(plain)的Java注解?
     *
     * @param type 待检查的类
     * @return 如果类名以java开头/@Ordered, 那么都只有简单注解, return true; 否则return false
     */
    @JvmStatic
    private fun hasPlainJavaAnnotationsOnly(type: Class<*>): Boolean {
        return type.name.startsWith("java.") || type == Ordered::class.java
    }

    /**
     * 检查这个注解是否可以被忽略掉
     *
     * @param annotationType annotationType
     * @return 如果它被Plain的AnnotationFilter匹配上了, return true需要忽略掉; 否则return false不需要忽略掉
     */
    @JvmStatic
    private fun isIgnorable(annotationType: Class<*>): Boolean = AnnotationFilter.PLAIN.matches(annotationType)

    /**
     * 检查给定的元素是否是只是含有一些简单(plain)的Java注解?
     *
     * @param element 待检查的元素(方法/字段/类/构造器等)
     * @return 如果该元素只要一些简单的Java注解的话, return true; 否则return false
     */
    @JvmStatic
    fun hasPlainJavaAnnotationsOnly(@Nullable element: AnnotatedElement?): Boolean {
        if (element is Class<*>) {
            return hasPlainJavaAnnotationsOnly(element)
        } else if (element is Member) {
            return hasPlainJavaAnnotationsOnly(element.declaringClass)
        }
        return false
    }

    /**
     * 清除缓存
     */
    @JvmStatic
    fun clearCache() {
        this.declaredAnnotationCache.clear()
    }
}