package com.wanna.framework.beans

/**
 * PropertyEditorRegistrar, 可以使用自定义的逻辑, 将PropertyEditor添加到PropertyEditorRegistry当中
 */
@FunctionalInterface
interface PropertyEditorRegistrar {
    fun registerPropertyEditor(registry: PropertyEditorRegistry)
}