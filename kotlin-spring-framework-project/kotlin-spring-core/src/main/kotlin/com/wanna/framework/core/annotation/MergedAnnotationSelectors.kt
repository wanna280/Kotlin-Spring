package com.wanna.framework.core.annotation

/**
 * 提供MergedAnnotationSelector相关的实例的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/31
 *
 * @see MergedAnnotationSelector
 */
object MergedAnnotationSelectors {

    /**
     * 选取Distance最近的一个MergedAnnotation作为要去进行使用MergedAnnotation的Selector实例
     */
    @JvmStatic
    private val NEAREST: MergedAnnotationSelector<*> = Nearest()

    /**
     * 选取第一个直接定义的注解去作为最终的MergedAnnotation的Selector实例
     */
    @JvmStatic
    private val FIRST_DIRECTLY_DECLARED = FirstDirectlyDeclared()

    /**
     * 选取Distance最近的一个MergedAnnotation作为要去进行使用MergedAnnotation的Selector实例
     *
     * @param A AnnotationType
     * @return 选取Distance最近的一个MergedAnnotation作为要去进行使用MergedAnnotation的Selector实例
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <A : Annotation> nearest(): MergedAnnotationSelector<A> = this.NEAREST as MergedAnnotationSelector<A>


    /**
     * 选取第一个直接定义的注解去作为最终的MergedAnnotation的Selector实例
     *
     * @param A AnnotationType
     * @return 选取第一个直接定义的注解去作为最终的MergedAnnotation的Selector实例
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <A : Annotation> firstDirectlyDeclared(): MergedAnnotationSelector<A> =
        this.FIRST_DIRECTLY_DECLARED as MergedAnnotationSelector<A>

    /**
     * 选取Distance最近的一个MergedAnnotation作为要去进行使用MergedAnnotation的Selector
     */
    private class Nearest : MergedAnnotationSelector<Annotation> {

        /**
         * 如果给定的注解的distance=0, 说明是直接标注的注解, 那么是最好的选择
         *
         * @param annotation annotation
         * @return 如果该Annotation对应的distance=0, return true; 否则return false
         */
        override fun <A : Annotation> isBestCandidate(annotation: MergedAnnotation<A>) = annotation.distance == 0

        /**
         * 从给定的两个MergedAnnotation当中去选取出来一个合适的MergedAnnotation
         *
         * @param existing 之前已经存在的MergedAnnotation
         * @param candidate 遇到的新的MergedAnnotation
         * @return 两者之中distance最小的一个Annotation
         */
        override fun select(
            existing: MergedAnnotation<Annotation>, candidate: MergedAnnotation<Annotation>
        ): MergedAnnotation<Annotation> {
            return if (candidate.distance < existing.distance) candidate else existing
        }
    }

    /**
     * 选取第一个直接定义的注解去作为最终的MergedAnnotation的Selector
     */
    private class FirstDirectlyDeclared : MergedAnnotationSelector<Annotation> {

        /**
         * 如果给定的注解的distance=0, 说明是直接标注的注解, 那么是最好的选择
         *
         * @param annotation annotation
         * @return 如果该Annotation对应的distance=0, return true; 否则return false
         */
        override fun <A : Annotation> isBestCandidate(annotation: MergedAnnotation<A>) = annotation.distance == 0

        override fun select(
            existing: MergedAnnotation<Annotation>,
            candidate: MergedAnnotation<Annotation>
        ): MergedAnnotation<Annotation> {
            if (existing.distance > 0 && candidate.distance == 0) {
                return candidate
            }
            return existing
        }
    }
}