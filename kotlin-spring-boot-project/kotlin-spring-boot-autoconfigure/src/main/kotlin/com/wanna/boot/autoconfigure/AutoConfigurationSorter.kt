package com.wanna.boot.autoconfigure

import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.classreading.MetadataReaderFactory
import com.wanna.framework.lang.Nullable
import java.io.IOException
import java.util.*

/**
 * 完成SpringBoot的AutoConfiguration自动配置类的排序，主要处理`@AutoConfigureAfter`/`@AutoConfigureBefore`等注解;
 * Note: 这里不能去进行任何的类加载, 因为这里所处的时机比较早, 还不应该产生类加载的行为, 因此采用的方式是MetadataReader,
 * 利用ASM的方式去读取自动配置类的相关信息(比如`@AutoConfigureBefore`/`@AutoConfigureAfter`/`@AutoConfigureOrder`)
 *
 * @param autoConfigurationMetadata AutoConfiguration Metadata
 * @param metadataReaderFactory MetadataReaderFactory
 *
 * @see AutoConfigureOrder
 * @see AutoConfigureBefore
 * @see AutoConfigureAfter
 */
open class AutoConfigurationSorter(
    private val autoConfigurationMetadata: AutoConfigurationMetadata,
    private val metadataReaderFactory: MetadataReaderFactory
) {
    /**
     * 按照优先级对于列表当中的自动配置类去进行排序之后的结果
     *
     * @param classNames classNames
     * @return 对于给定的配置类去进行排序之后的结果
     */
    open fun getInPriorityOrder(classNames: Collection<String>): List<String> {
        // 构建出来AutoConfigurationClasses
        val classes = AutoConfigurationClasses(metadataReaderFactory, autoConfigurationMetadata, classNames)

        // 排好序的自动配置类ClassName列表
        var orderedClassNames: MutableList<String> = ArrayList(classNames)

        // 1.先按照自动配置类的字母的顺序去进行排序
        orderedClassNames.sort()

        // 2.按照@AutoConfigureOrder去进行排序
        orderedClassNames.sortWith(Comparator { o1, o2 ->
            val order1 = classes.get(o1)!!.getOrder()  // assert contains
            val order2 = classes.get(o2)!!.getOrder()  // assert contains
            return@Comparator order1.compareTo(order2)
        })

        // 3.检查@AutoConfigureBefore/@AutoConfigureAfter, 去进行顺序的调整...
        orderedClassNames = sortByAnnotation(classes, orderedClassNames)
        return orderedClassNames
    }

    /**
     * 按照@AutoConfigureBefore/@AutoConfigureAfter去进行顺序的调整...
     */
    private fun sortByAnnotation(
        classes: AutoConfigurationClasses, classNames: MutableList<String>
    ): MutableList<String> {

        // 初始化要去进行排序的className列表
        val toSort = ArrayList(classNames)
        // 添加一些额外的className, 包括自动配置类当中的before/after
        toSort.addAll(classes.getAllNames())

        val sorted = LinkedHashSet<String>()
        val processing = LinkedHashSet<String>()

        // 针对所有的自动配置类, 去执行排序
        while (toSort.isNotEmpty()) {
            doSortByAfterAnnotation(classes, toSort, sorted, processing, null)
        }
        sorted.retainAll(classNames.toSet())
        return ArrayList(sorted)
    }

    /**
     * 针对某个自动配置类, 去执行递归的排序(将一个配置类依赖的配置类, 提前去进行收集到sorted集合当中)
     *
     * @param classes 所有的自动配置类的装配顺序信息
     * @param toSort 正在进行排序的自动配置类的队列
     * @param sorted 已经完成排序的自动配置类列表(输出参数, 存放最终的排序结果)
     * @param processing 正在进行处理的自动配置类列表
     * @param current 正在执行排序的自动配置类的className
     */
    private fun doSortByAfterAnnotation(
        classes: AutoConfigurationClasses,
        toSort: MutableList<String>,
        sorted: MutableSet<String>,
        processing: MutableSet<String>,
        @Nullable current: String?
    ) {
        // 如果current=null, 那么从要去进行排序的自动配置类队列当中拿出来一个
        val currentClassName = current ?: toSort.removeAt(0)

        // current添加到正在进行处理的配置类队列当中, 也就是回溯的添加操作
        processing += currentClassName

        // 如果存在有装配该自动配置类之前, 要去进行先装配的配置类的话, 那么先把它们加入到队列当中
        for (after in classes.getClassesRequestedAfter(currentClassName)) {

            // 检查循环引用的情况
            checkForCycles(processing, currentClassName, after)

            // 如果after这个元素在之前没有被排好序的话, 那么需要以after作为current, 去进行递归处理, 完成排序
            if (!sorted.contains(after) && toSort.contains(after)) {
                doSortByAfterAnnotation(classes, toSort, sorted, processing, after)
            }
        }

        // 处理完之后, 将current从正在处理的配置类队列当中去进行移除...也就是回溯的撤销操作
        processing -= currentClassName

        // 在处理完成所有的预先装配的配置类之后, 把current收到队列当中来
        sorted += currentClassName
    }

    private fun checkForCycles(processing: Set<String>, current: String, after: String) {
        if (processing.contains(after)) {
            throw IllegalStateException("AutoConfigure cycle detected between $current and $after")
        }
    }

    private class AutoConfigurationClasses(
        metadataReaderFactory: MetadataReaderFactory,
        autoConfigurationMetadata: AutoConfigurationMetadata,
        classNames: Collection<String>
    ) {
        /**
         * 自动配置类的装配顺序信息, Key-className, Value-该自动配置类的装配优先级/Before/After信息
         */
        private val classes = LinkedHashMap<String, AutoConfigurationClass>()

        init {
            // 将给定的classNames去添加到classes列表当中去
            addToClasses(metadataReaderFactory, autoConfigurationMetadata, classNames, true)
        }

        /**
         * 根据自动配置类类名去获取该自动配置类的装配顺序信息
         *
         * @return AutoConfigurationClass(如果不存在的话, return null)
         */
        @Nullable
        fun get(className: String): AutoConfigurationClass? = classes[className]

        fun getAllNames(): Set<String> = this.classes.keys

        /**
         * 将给定的这些自动配置类添加到classes列表当中去
         *
         * @param metadataReaderFactory MetadataReaderFactory
         * @param autoConfigurationMetadata AutoConfigurationMetadata
         * @param classNames 要去进行添加的自动配置类类名
         * @param required 该自动配置类是否是必须的?
         */
        private fun addToClasses(
            metadataReaderFactory: MetadataReaderFactory,
            autoConfigurationMetadata: AutoConfigurationMetadata,
            classNames: Collection<String>,
            required: Boolean
        ) {
            for (className in classNames) {
                if (classes.containsKey(className)) {
                    continue
                }
                val autoConfigurationClass =
                    AutoConfigurationClass(className, autoConfigurationMetadata, metadataReaderFactory)
                // 检查这个配置类是否可用?(是否存在于当前VM当中?)
                val available = autoConfigurationClass.isAvailable()
                if (required || available) {
                    classes[className] = autoConfigurationClass
                }
                // 如果该自动配置类存在的话, 那么把它的before/after的配置类也添加到classes列表当中来
                if (available) {
                    addToClasses(
                        metadataReaderFactory, autoConfigurationMetadata, autoConfigurationClass.getBefore(), false
                    )
                    addToClasses(
                        metadataReaderFactory, autoConfigurationMetadata, autoConfigurationClass.getAfter(), false
                    )
                }
            }
        }

        /**
         * 检查请求的className的自动配置类, 需要在哪些自动配置类都完成装配之后, 才能完成装配?
         * 对于一个类A来说, 主要包含两部分:
         * * 1.A通过@AutoConfigureAfter配置的那些配置类
         * * 2.别的类B/C通过@AutoConfigureBefore配置了类A的那些配置类
         *
         * @param className request className
         * @return 该自动配置类需要等哪些自动配置类完成装配之后, 才能完成装配?
         */
        fun getClassesRequestedAfter(className: String): Set<String> {
            // 先检查它配置的After
            val classesRequestedAfter = LinkedHashSet(get(className)!!.getAfter())

            // 检查还有哪些自动配置类的Before当中配置了该className?
            this.classes.forEach { (name, classes) ->
                if (classes.getBefore().contains(name)) {
                    classesRequestedAfter += name
                }
            }
            return classesRequestedAfter
        }
    }

    /**
     * 描述一个自动配置类的装配顺序
     *
     * @param className 自动配置类的className
     * @param autoConfigurationMetadata AutoConfigurationMetadata
     * @param metadataReaderFactory MetadataReaderFactory
     */
    private class AutoConfigurationClass(
        private val className: String,
        private val autoConfigurationMetadata: AutoConfigurationMetadata,
        private val metadataReaderFactory: MetadataReaderFactory
    ) {
        @Volatile
        @Nullable
        private var before: Set<String>? = null

        @Volatile
        @Nullable
        private var after: Set<String>? = null

        @Nullable
        private var annotationMetadata: AnnotationMetadata? = null

        /**
         * 检查该自动配置类是否存在于当前JVM当中?
         *
         * @return 如果存在的话, return true; 不存在的话, return false
         */
        fun isAvailable(): Boolean {
            return try {
                if (!wasProcessed()) {
                    getAnnotationMetadata()
                }
                true
            } catch (ex: Throwable) {
                false
            }
        }


        /**
         * 获取到该配置类配置的AutoConfigureBefore信息
         *
         * @return before AnnotationConfiguration Classes
         */
        fun getBefore(): Set<String> {
            if (this.before == null) {
                if (wasProcessed()) {
                    this.before = this.autoConfigurationMetadata.getSet(
                        this.className, "AutoConfigureBefore", Collections.emptySet()
                    )
                } else {
                    before = getAnnotationValue(AutoConfigureBefore::class.java)
                }
            }
            return before ?: emptySet()
        }

        /**
         * 获取到该配置类配置的AutoConfigureAfter信息
         *
         * @return after AutoConfiguration Classes
         */
        fun getAfter(): Set<String> {
            if (this.after == null) {
                if (wasProcessed()) {
                    this.after = this.autoConfigurationMetadata.getSet(
                        this.className, "AutoConfigureAfter", Collections.emptySet()
                    )
                } else {
                    this.after = getAnnotationValue(AutoConfigureAfter::class.java)
                }
            }
            return after ?: emptySet()
        }

        /**
         * 获取到该自动配置类的Order
         *
         * @return order
         */
        fun getOrder(): Int {
            if (wasProcessed()) {
                return this.autoConfigurationMetadata.getInt(
                    this.className, "AutoConfigureOrder", AutoConfigureOrder.DEFAULT_ORDER
                )
            }
            val mergedAnnotation = getAnnotationMetadata().getAnnotations().get(AutoConfigureOrder::class.java)
            return if (mergedAnnotation.present) mergedAnnotation.getInt("value") else AutoConfigureOrder.DEFAULT_ORDER
        }

        /**
         * 检查当前配置类的className是否已经被处理过了?
         *
         * @return 如果已经被处理过, return true, 不用去进行继续处理; 否则return false
         */
        private fun wasProcessed(): Boolean = this.autoConfigurationMetadata.wasProcessed(className)

        /**
         * 获取AnnotationMetadata当中给定的注解类型的属性值
         *
         * @param annotation 要去获取的注解类型
         * @return 该类型的value当中配置的属性值
         */
        private fun getAnnotationValue(annotation: Class<*>): Set<String> {
            val mergedAnnotation = getAnnotationMetadata().getAnnotations().get<Annotation>(annotation.name)
            if (!mergedAnnotation.present) {
                return Collections.emptySet()
            }
            val value = LinkedHashSet<String>()
            value.addAll(mergedAnnotation.getStringArray("value"))
            value.addAll(mergedAnnotation.getStringArray("name"))
            return value
        }

        /**
         * 根据MetadataReaderFactory, 去读取到该类的注解元信息AnnotationMetadata
         *
         * @return 当前自动配置类的注解元信息AnnotationMetadata
         * @throws IllegalStateException 如果当前VM环境当中不存在有这样的自动配置类的依赖
         */
        @Throws(IllegalStateException::class)
        private fun getAnnotationMetadata(): AnnotationMetadata {
            if (this.annotationMetadata == null) {
                try {
                    this.annotationMetadata = metadataReaderFactory.getMetadataReader(className).annotationMetadata
                } catch (ex: IOException) {
                    throw IllegalStateException("Unable to read meta-data for class $className", ex)
                }
            }
            return this.annotationMetadata!!
        }
    }
}