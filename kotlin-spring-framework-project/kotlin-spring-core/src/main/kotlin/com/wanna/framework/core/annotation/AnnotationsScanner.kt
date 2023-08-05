package com.wanna.framework.core.annotation

import com.wanna.framework.core.Ordered
import com.wanna.framework.core.annotation.MergedAnnotations.SearchStrategy
import com.wanna.framework.core.annotation.MergedAnnotations.SearchStrategy.*
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ReflectionUtils
import java.lang.annotation.Inherited
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 注解扫描的Scanner工具类, 用于提供[AnnotatedElement]的继承关系的相关扫描功能
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

    /**
     * BaseType的Methods缓存, Key-Class, Value-该类上的所有的有标注注解的方法(没有注解的不考虑, 该位置的元素将会设置为null)
     */
    @JvmStatic
    private val baseTypeMethodsCache = ConcurrentHashMap<Class<*>, Array<Method?>>()

    /**
     * 从给定的[AnnotatedElement]上, 利用给定的[AnnotationsProcessor]去进行注解的搜索和处理
     *
     * @param context context
     * @param source source AnnotatedElement(Field/Method/Other)
     * @param searchStrategy 注解的搜索策略
     * @param processor 对注解去进行处理和收集的Processor
     * @return 利用Processor去进行处理注解的结果(or null)
     */
    @Nullable
    @JvmStatic
    fun <C, R> scan(
        context: C,
        source: AnnotatedElement,
        searchStrategy: SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {

        // 先对AnnotatedElement去进行分类型(类/方法/其他)处理
        val result = process(context, source, searchStrategy, processor)

        // 在处理完成之后, 使用finish方法去进行收尾工作的处理
        return processor.finish(result)
    }

    /**
     * 对于给定的[AnnotatedElement], 利用给定的[AnnotationsProcessor]去进行注解的搜索和处理
     *
     * @param context context
     * @param source source AnnotatedElement(Field/Method/Other)
     * @param searchStrategy 注解的搜索策略
     * @param processor 对注解去进行处理的Processor
     * @return 使用Processor去对注解去进行处理的结果
     */
    @Nullable
    @JvmStatic
    private fun <C, R> process(
        context: C,
        source: AnnotatedElement,
        searchStrategy: SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return when (source) {
            is Class<*> -> processClass(context, source, searchStrategy, processor)
            is Method -> processMethod(context, source, searchStrategy, processor)
            else -> processElement(context, source, processor)
        }
    }

    /**
     * 根据[SearchStrategy]的不同, 选用合适的方式去处理类, 检查是否有标注该注解?
     *
     * @param context context
     * @param source sourceClass(要去进行处理的类)
     * @param searchStrategy 注解的搜索策略
     * @param processor 对注解去进行处理的Processor
     * @return 根据Processor去进行处理的搜索结果
     */
    @Nullable
    @JvmStatic
    private fun <C, R> processClass(
        context: C,
        source: Class<*>,
        searchStrategy: SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return when (searchStrategy) {

            // 如果SearchStrategy=DIRECT, 那么只处理给定的类上的注解, 直接使用AnnotatedElement的处理方式即可
            DIRECT ->
                processElement(context, source, processor)

            // 如果SearchStrategy=INHERITED_ANNOTATIONS, 需要处理继承的注解
            INHERITED_ANNOTATIONS ->
                processClassInheritedAnnotations(context, source, searchStrategy, processor)

            // 如果SearchStrategy=SUPERCLASS, 那么需要处理目标类, 以及它的所有父类上的注解
            SUPERCLASS ->
                processClassHierarchy(context, IntArray(1), source, processor, false, false)

            // 如果SearchStrategy=TYPE_HIERARCHY, 那么需要处理目标类, 以及它的所有父类/所有接口上的注解
            TYPE_HIERARCHY ->
                processClassHierarchy(context, IntArray(1), source, processor, true, false)

            // 如果SearchStrategy=TYPE_HIERARCHY_AND_ENCLOSING_CLASSES, 需要在TYPE_HIERARCHY的基础上, 新增去去处理外部类上的注解...
            TYPE_HIERARCHY_AND_ENCLOSING_CLASSES ->
                processClassHierarchy(context, IntArray(1), source, processor, true, true)
        }
    }

    /**
     * 按照不同的SearchStrategy, 采用不同的方式对给定的方法去进行注解的搜索处理
     *
     * @param context context
     * @param source 要去进行搜索的方法
     * @param searchStrategy 注解的搜索策略
     * @param processor 对注解去进行处理和收集的Processor
     * @return 处理注解的Processor对于注解的的处理结果
     */
    @Nullable
    @JvmStatic
    private fun <C, R> processMethod(
        context: C,
        source: Method,
        searchStrategy: SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return when (searchStrategy) {
            // 如果是SearchStrategy=DIRECT/INHERITED_ANNOTATIONS的话, 那么处理方法继承的注解...
            DIRECT, INHERITED_ANNOTATIONS -> processMethodInheritedAnnotations(context, source, processor)

            // 如果是SearchStrategy=SUPERCLASS的话, 那么只处理SuperClass, includeInterfaces=false
            SUPERCLASS -> processMethodHierarchy(context, IntArray(1), source.declaringClass, processor, source, false)

            // 如果SearchStrategy=TYPE_HIERARCHY的话, 需要处理类型继承(TYPE_HIERARCHY)情况, 因此includeInterfaces=false
            TYPE_HIERARCHY, TYPE_HIERARCHY_AND_ENCLOSING_CLASSES -> processMethodHierarchy(
                context, IntArray(1), source.declaringClass, processor, source, true
            )
        }
    }

    /**
     * 带注解的继承关系地去处理一个类, 支持去处理[Inherited]注解这种注解的继承方式(JDK原生支持去处理[Inherited]注解, 无需我们额外处理),
     * 支持找到目标类上的所有的(直接/继承)注解, 从它以及它的所有父类当中去找到原始的注解, 并交给[AnnotationsProcessor]去进行注解的处理
     *
     * Note: [AnnotatedElement.getAnnotations]支持去获取到使用[Inherited]去进行继承父类的注解,
     * 而对于[AnnotatedElement.getDeclaredAnnotations]这个方法来说, 只能获取到自己这个类去定义的那些注解
     *
     * @param context context
     * @param source sourceClass
     * @param searchStrategy 注解的搜索策略(应该为INHERITED_ANNOTATIONS)
     * @param processor 对注解去进行处理和收集的Processor
     * @return Processor去处理的结果, 如果没有处理出来结果, 那么return null
     */
    @Suppress("UNCHECKED_CAST")
    @Nullable
    @JvmStatic
    private fun <C, R> processClassInheritedAnnotations(
        context: C,
        source: Class<*>,
        searchStrategy: SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        try {
            // 如果确定source一定没有继承关系了, 那么直接使用Element的方式去进行处理即可
            if (isWithoutHierarchy(source, searchStrategy)) {
                return processElement(context, source, processor)
            }
            // 记录的是source当中的注解, 包含有一些很基础的注解
            var relevant: Array<Annotation?>? = null

            // 统计剩下的还需要去进行处理的注解的数量...
            var remaining = Int.MAX_VALUE
            var aggregateIndex = 0

            // 获取到sourceClass当中的所有的(直接/继承)注解, 从它的parentClass当中, 去找到类型一样的注解...
            // 并交给Processor去进行处理...
            var clazz: Class<*>? = source
            while (clazz != null && clazz != Any::class.java  // 如果没有父类了, pass
                && remaining > 0             // 如果没有注解都找到了匹配的, 那么直接pass
                && !hasPlainJavaAnnotationsOnly(clazz)  // 如果parentClass只有简单注解了, pass
            ) {
                var result = processor.doWithAggregate(context, aggregateIndex)
                if (result != null) {
                    return result
                }

                // 获取的是当前这个父类当中定义的所有的自己直接定义的注解
                val declaredAnnotations = getDeclaredAnnotations(clazz, true) as Array<Annotation?>

                // 初始化relevant为source的Annotations列表, remaining为relevant.size, 代表要去进行处理的剩下的注解个数
                if (relevant == null && declaredAnnotations.isNotEmpty()) {
                    // Note: getAnnotations, 获取得到的是所有的, 包含使用@Inherited去继承的父类的注解...
                    relevant = source.annotations
                    remaining = relevant!!.size
                }

                // 一层遍历, 遍历当前source的当前父类当中的所有的定义的注解
                for (index in declaredAnnotations.indices) {
                    var isRelevant = false

                    // 二层遍历, 遍历source的所有定义的注解
                    for (relevantIndex in 0 until relevant!!.size) {

                        // 如果source和source的当前父类的注解类型相同, 那么说明匹配成功, 对于父类当中的这个注解就需要去进行处理
                        if (relevant[relevantIndex] != null && declaredAnnotations[index] != null
                            && declaredAnnotations[index]!!.annotationClass.java == relevant[relevantIndex]!!.annotationClass.java
                        ) {
                            isRelevant = true
                            relevant[relevantIndex] = null

                            // 剩余要去进行处理的注解数量--
                            remaining--
                            break
                        }
                    }

                    // 如果当前父类, 和子类一个都匹不上, 那么这个注解没必要留着了, 设置为null直接干掉...
                    if (!isRelevant) {
                        declaredAnnotations[index] = null
                    }
                }

                // 从parentClass当中确实是有找到类型匹配的注解的话, 那么将此时对应的parentClass/parentAnnotations
                // 去交给给定的AnnotationsProcessor去进行处理
                result = processor.doWithAnnotations(
                    context, aggregateIndex, clazz,
                    declaredAnnotations.filterNotNull().toTypedArray()  // filter nonNull
                )
                if (result != null) {
                    return result
                }
                aggregateIndex++
                clazz = clazz.superclass
            }
        } catch (ex: Throwable) {
            AnnotationUtils.handleIntrospectionFailure(source, ex)
        }
        return null
    }

    /**
     * 检查给定的[AnnotatedElement]是否没有继承关系可以去进行处理?
     *
     * @param source source AnnotatedElement
     * @param searchStrategy 注解的搜索策略
     * @return 如果它一定没有继承关系了, return true; 只要可能存在有继承关系, return false
     */
    @JvmStatic
    private fun isWithoutHierarchy(source: AnnotatedElement, searchStrategy: SearchStrategy): Boolean {
        if (source == Any::class.java) {
            return true
        }

        // 如果source是Class的话, 那么检查一些父类/接口的情况
        if (source is Class<*>) {
            // 检查这个类是否一定没有父接口/父类了...(如果父类为Object, 并且它还没有接口, 那么说明一定没有superTypes了)
            val noSuperTypes = source.superclass == Any::class.java && source.interfaces.isEmpty()

            // 如果是TYPE_HIERARCHY_AND_ENCLOSING_CLASSES, 并且这个类一定没有父接口/父类的话,
            // 那么需要检查外部类, 如果enclosingClass=null的话, return true
            if (searchStrategy == TYPE_HIERARCHY_AND_ENCLOSING_CLASSES) {
                return noSuperTypes && source.enclosingClass == null
            }

            // 如果不是TYPE_HIERARCHY_AND_ENCLOSING_CLASSES的话, 那么直接检查noSuperTypes即可
            return noSuperTypes
        }

        // 如果source是Method的话, 那么检查declaringClass和Modifier
        if (source is Method) {
            return Modifier.isPrivate(source.modifiers) || isWithoutHierarchy(source.declaringClass, searchStrategy)
        }
        return true
    }

    /**
     * 带继承关系地去处理一个类, 根据给定的[AnnotationsProcessor]这个Callback方法去检查是否给定的这些注解是否符合需要
     *
     * * 1.先检查类本身是否有标注该注解?
     * * 2.再检查父类上是否有标注该注解?
     * * 3.接着检查接口上是否有标注该注解?
     * * 4.最后检查外部类上是否有标注该注解?
     * * 5.重复往上, 对于每个类都是递归去进行处理的...因此对于所有的父类/所有的接口/所有外部类, 都能被处理到
     *
     * @param context context
     * @param aggregateIndex aggregateIndex
     * @param processor 处理注解的Processor
     * @param includeEnclosing 是否要处理外部类?
     * @param includeInterfaces 是否要处理接口?
     * @return 使用给定的AnnotationsProcessor去处理注解的结果...
     */
    @Suppress("UNCHECKED_CAST")
    @Nullable
    @JvmStatic
    private fun <C, R> processClassHierarchy(
        context: C,
        aggregateIndex: IntArray,
        source: Class<*>,
        processor: AnnotationsProcessor<C, R>,
        includeInterfaces: Boolean,
        includeEnclosing: Boolean
    ): R? {
        try {
            var result = processor.doWithAggregate(context, aggregateIndex[0])
            if (result != null) {
                return result
            }

            // 如果只要简单的Java注解, return null
            if (hasPlainJavaAnnotationsOnly(source)) {
                return null
            }

            // 1.检查类上是否直接定义了该注解? 如果有的话, 直接return ...
            val declaredAnnotations = getDeclaredAnnotations(source, false)
            result = processor.doWithAnnotations(context, aggregateIndex[0], source, declaredAnnotations as Array<Annotation?>)
            if (result != null) {
                return result
            }
            aggregateIndex[0]++

            // 如果类上没有直接定义该注解, 那么得尝试从父类/接口当中去进行搜索

            // 2.先检查接口...
            if (includeInterfaces) {
                for (itf in source.interfaces) {
                    val interfaceResult = processClassHierarchy(
                        context, aggregateIndex, itf,
                        processor, includeInterfaces, includeEnclosing
                    )
                    if (interfaceResult != null) {
                        return interfaceResult
                    }
                }
            }
            // 3.再检查父类(递归对象Source=SuperClass)
            val superclass = source.superclass
            if (superclass != null && superclass != Any::class.java) {
                val superClassResult = processClassHierarchy(
                    context, aggregateIndex, superclass,
                    processor, includeInterfaces, includeEnclosing
                )
                if (superClassResult != null) {
                    return superClassResult
                }
            }

            // 4.最后检查外部类(EnclosingClass)
            if (includeEnclosing) {
                val enclosingClass = source.enclosingClass
                if (enclosingClass != null) {
                    try {
                        val enclosingResult = processClassHierarchy(
                            context, aggregateIndex, enclosingClass,
                            processor, includeInterfaces, includeEnclosing
                        )
                        if (enclosingResult != null) {
                            return enclosingResult
                        }
                    } catch (ex: Throwable) {
                        AnnotationUtils.handleIntrospectionFailure(source, ex)
                    }
                }
            }
        } catch (ex: Throwable) {
            AnnotationUtils.handleIntrospectionFailure(source, ex)
        }
        return null
    }

    /**
     * 带继承关系地去处理一个方法, 如果一个方法存在有Override重写的情况的话,
     * 那么需要从sourceClass所有的父类/接口当中尝试去寻找到相同签名的方法, 去进行注解的寻找...
     *
     * @param context context(requiredType)
     * @param aggregateIndex aggregateIndex
     * @param sourceClass 正在处理的方法的源类, 将会从它的parentClass/interfaces当中去进行寻找注解
     * @param includeInterfaces 是否需要处理接口方法?
     * @param processor 需要对注解去进行收集的Processor Callback方法
     * @param rootMethod 正在处理的方法
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

            // 如果该类只存在有一些简单的Java注解, return null...
            if (hasPlainJavaAnnotationsOnly(sourceClass)) {
                return null
            }

            // 是否已经call了processor的标志位
            var calledProcessor = false

            // 如果sourceClass==rootMethod.declaringClass, 说明是这个类当中定义的该方法, 那么优先去进行处理, 那么在这里去执行该方法上的处理...
            if (sourceClass == rootMethod.declaringClass) {
                result = processMethodAnnotations(context, rootMethod, aggregateIndex[0], processor)
                calledProcessor = true

                // 如果从该方法上去寻找到了合适的注解的话, 那么return...没有找到的话, 继续递归寻找
                if (result != null) {
                    return result
                }

                // 如果sourceClass不是rootMethod.declaringClass, 那么尝试获取sourceClass当中的所有的方法,
                // 去进行检查两者之间是否是Override的关系? (方法名&方法参数类型都相同)
                // Spring对于继承的方法的寻找, 这种实现方法, 属实是比较奇妙...
            } else {
                for (candidateMethod in getBaseTypeMethods(context, sourceClass)) {

                    // 如果candidate方法和rootMethod的签名完全相同...那么尝试对该方法去进行处理
                    if (candidateMethod != null && isOverride(rootMethod, candidateMethod)) {
                        result = processMethodAnnotations(context, candidateMethod, aggregateIndex[0], processor)
                        calledProcessor = true

                        // 如果从该方法上去寻找到了合适的注解的话, 那么return...没有找到的话, 继续递归寻找
                        if (result != null) {
                            return result
                        }
                    }
                }
            }

            // 如果root方法是private, return null
            if (Modifier.isPrivate(rootMethod.modifiers)) {
                return null
            }

            // 如果已经交给AnnotationsProcessor处理过, 但是还是没有结果的话...
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
                // 利用父类去进行递归处理, 因此能够支持处理间接父类的情况...也能处理间接接口的情况...
                val superClassResult = processMethodHierarchy(
                    context, aggregateIndex, superclass, processor, rootMethod, includeInterfaces
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
     * 检查rootMethod和candidateMethod之间是否是可以去进行覆盖的?
     *
     * @param rootMethod rootMethod
     * @param candidateMethod candidateMethod
     */
    @JvmStatic
    private fun isOverride(rootMethod: Method, candidateMethod: Method): Boolean {
        return !Modifier.isPrivate(candidateMethod.modifiers)
                && candidateMethod.name == rootMethod.name
                && hasSameParameterTypes(rootMethod, candidateMethod)
    }

    /**
     * 检查给定的两个方法是否有相同的方法参数?
     *
     * @param rootMethod rootMethod
     * @param candidateMethod candidateMethod
     */
    @JvmStatic
    private fun hasSameParameterTypes(rootMethod: Method, candidateMethod: Method): Boolean {
        if (rootMethod.parameterCount != candidateMethod.parameterCount) {
            return false
        }
        // 这里理论上应该检查泛型的, 这里暂时不检查...
        return Arrays.equals(rootMethod.parameterTypes, candidateMethod.parameterTypes)
    }

    /**
     * 处理方法继承的注解
     *
     * @param context context(requiredType)
     * @param source 要去进行处理的目标方法
     * @param processor AnnotationsProcessor
     */
    @JvmStatic
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

    /**
     * 处理给定的目标方法身上的注解, 利用[AnnotationsProcessor]去进行处理得到结果...
     *
     * @param context context
     * @param source source
     * @param aggregateIndex aggregateIndex
     * @param processor 对注解去进行处理(例如收集)的Processor, 是一个Callback方法
     * @return 如果AnnotationsProcessor找到了合适的注解, return result; 否则return null
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @Nullable
    private fun <C, R> processMethodAnnotations(
        context: C, source: Method, aggregateIndex: Int, processor: AnnotationsProcessor<C, R>
    ): R? {
        // 获取目标方法上定义的直接注解列表
        val annotations = getDeclaredAnnotations(source, false)

        // 利用该方法身上的注解, 利用AnnotationsProcessor去进行处理
        val result = processor.doWithAnnotations(context, aggregateIndex, source, annotations as Array<Annotation?>)
        if (result != null) {
            return result
        }
        return null
    }

    /**
     * 处理一个AnnotatedElement上的注解
     */
    @Suppress("UNCHECKED_CAST")
    @Nullable
    @JvmStatic
    private fun <C, R> processElement(
        context: C,
        source: AnnotatedElement,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return processor.doWithAggregate(context, 0) ?: processor.doWithAnnotations(
            context, 0, source, getDeclaredAnnotations(source, false) as Array<Annotation?>
        )
    }

    /**
     * 获取到目标元素身上的所有的直接定义的注解(过滤掉了一些不需要的元注解)
     *
     * @param source source
     * @param defensive 是否具有侵入性? 如果具有侵入性的话, 那么需要clone一份去进行返回
     * @return declaredAnnotations(去掉了不需要的那些Meta注解)
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

    /**
     * 获取给定的目标的类(baseType)上的全部有标注注解的方法(没有注解的方法我们不需要...)
     *
     * @param context context
     * @param baseType baseType, 要去进行寻找方法的类
     * @return baseType类上的全部有注解的方法(如果数组当中遇到了没有注解标注的方法的话, 会被设为null)
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    private fun <C> getBaseTypeMethods(context: C, baseType: Class<*>): Array<Method?> {
        // 如果给定的baseType为Object, 或者baseType上只有一些简单Java注解的话, 那么return empty
        if (baseType == Any::class.java || hasPlainJavaAnnotationsOnly(baseType)) {
            return NO_METHODS as Array<Method?>
        }
        var methods = baseTypeMethodsCache[baseType]
        if (methods === null) {
            val isInterface = baseType.isInterface
            // 如果是接口直接getMethods, 如果是类的话, 获取它定义的所有方法(declaredMethods+defaultMethods)
            methods =
                if (isInterface) baseType.methods else ReflectionUtils.getDeclaredMethods(baseType) as Array<Method?>

            var cleared = 0

            // 1.如果它是一个类上的Private方法, 那么该方法不要
            // 2.如果该类上只有简单的Java注解, 或者没有注解, 那么不要...
            methods!!.indices.forEach {
                if ((!isInterface && Modifier.isPrivate(methods[it]!!.modifiers))
                    || hasPlainJavaAnnotationsOnly(methods[it])
                    || getDeclaredAnnotations(methods[it]!!, false).isEmpty()
                ) {
                    methods[it] = null
                    cleared++
                }
            }
            if (cleared == methods.size) {
                return NO_METHODS as Array<Method?>
            }
            baseTypeMethodsCache[baseType] = methods
        }
        return methods
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
     *
     * @see declaredAnnotationCache
     * @see baseTypeMethodsCache
     */
    @JvmStatic
    fun clearCache() {
        this.declaredAnnotationCache.clear()
        this.baseTypeMethodsCache.clear()
    }
}