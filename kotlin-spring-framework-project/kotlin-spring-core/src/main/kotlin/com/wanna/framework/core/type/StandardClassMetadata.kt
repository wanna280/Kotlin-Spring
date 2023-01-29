package com.wanna.framework.core.type

import com.wanna.framework.lang.Nullable
import java.lang.reflect.Modifier

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
open class StandardClassMetadata(val introspectedClass: Class<*>) : ClassMetadata {

    override fun getClassName(): String = introspectedClass.name

    override fun getPackageName(): String = introspectedClass.packageName

    override fun isInterface(): Boolean = introspectedClass.isInterface

    override fun isAnnotation(): Boolean = introspectedClass.isAnnotation

    override fun isAbstract(): Boolean = Modifier.isAbstract(introspectedClass.modifiers)

    override fun isConcrete(): Boolean = !isConcrete()

    override fun isFinal(): Boolean = Modifier.isFinal(introspectedClass.modifiers)

    override fun hasEnclosingClass(): Boolean = introspectedClass.enclosingClass != null

    @Nullable
    override fun getEnclosingClassName(): String? = introspectedClass.enclosingClass.name

    override fun hasSuperClass(): Boolean = introspectedClass.superclass != null

    @Nullable
    override fun getSuperClassName(): String? = introspectedClass.superclass.name

    override fun getInterfaceNames(): Array<String> = introspectedClass.interfaces.map { it.name }.toTypedArray()

    override fun getMemberClassNames(): Array<String> = introspectedClass.declaredClasses.map { it.name }.toTypedArray()

    override fun isIndependentInnerClass(): Boolean =
        hasEnclosingClass() && Modifier.isStatic(introspectedClass.modifiers)
}