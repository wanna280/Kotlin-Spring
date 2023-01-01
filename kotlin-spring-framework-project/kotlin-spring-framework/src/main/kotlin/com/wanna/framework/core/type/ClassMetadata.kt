package com.wanna.framework.core.type

/**
 * 这是维护一个类的元信息
 *
 * @see StandardAnnotationMetadata
 */
interface ClassMetadata {

    /**
     * 获取该类的类名
     *
     * @return className
     */
    fun getClassName(): String

    /**
     * 获取该类的包名
     *
     * @return packageName
     */
    fun getPackageName(): String

    /**
     * 判断该类是否是一个接口?
     *
     * @return 如果是一个接口，return true; 否则return false
     */
    fun isInterface(): Boolean

    /**
     * 当前类是否是一个注解?
     *
     * @return 如果是一个注解, return true; 否则return false
     */
    fun isAnnotation(): Boolean

    /**
     * 当前类是否是抽象的?
     *
     * @return 如果是抽象的类, return true; 否则return false
     */
    fun isAbstract(): Boolean

    /**
     * 当前类是否是一个具体的类? (非抽象类/非接口)
     *
     * @return 如果是一个具体的类, return true; 否则return false
     */
    fun isConcrete(): Boolean

    /**
     * 当前类是否是final的?
     *
     * @return 如果是final的, return true; 否则return false
     */
    fun isFinal(): Boolean

    /**
     * 当前是否存在有外部类? (对于inner class/ nested class都存在有外部类, 或者是定义在方法内部的类, 也就是匿名内部类, 也存在有外部类)
     *
     * @return 如果它存在有外部类, return true; 否则return false
     */
    fun hasEnclosingClass(): Boolean

    /**
     * 是否是一个独立的内部类? (是否是一个静态内部类)
     *
     * @return 如果是独立的内部类, return true; 否则return false
     */
    fun isIndependentInnerClass(): Boolean

    /**
     * 获取当前类对应的外部类
     *
     * @return 外部类的类名(不存在有外部类的话, return null)
     */
    fun getEnclosingClassName(): String?

    /**
     * 当前类是否存在有父类?
     *
     * @return 如果当前类有父类的话, 那么return true; 否则return false
     */
    fun hasSuperClass(): Boolean

    /**
     * 获取superClassName
     *
     * @return superClassName
     */
    fun getSuperClassName(): String?

    /**
     * 获取一个类的接口的类名列表
     *
     * @return 接口的类名列表
     */
    fun getInterfaceNames(): Array<String>

    /**
     * 获取成员类的类名列表
     *
     * @return 成员类的类名列表
     */
    fun getMemberClassNames(): Array<String>
}