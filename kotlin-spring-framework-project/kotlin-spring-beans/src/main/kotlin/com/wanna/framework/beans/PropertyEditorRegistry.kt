package com.wanna.framework.beans

import com.wanna.framework.core.convert.converter.Converter
import java.beans.PropertyEditor

/**
 * ## PropertyEditorRegistry是什么?
 *
 * [PropertyEditorRegistry]翻译成为中文叫做: "属性值编辑器"的注册中心;
 * 在Spring当中, 一个[PropertyEditor]的作用和一个[Converter]的作用基本类似, 主要是完成类型的转换工作;
 * [PropertyEditor]来自于Jdk的Desktop模块, 而[Converter]来自于Spring家的规范;
 * 在Spring当中, 凡是要用到类型的转换工作的, 基本上都会使用到[PropertyEditor]/[Converter]去完成;
 *
 * ## PropertyEditorRegistry的具体具体实现包括哪些?
 *
 * 典型的[PropertyEditorRegistry], 包括两个常见的实现: BeanWrapper(BeanWrapperImpl)和SimpleTypeConverter;
 * * 1.[BeanWrapper]组合[PropertyEditor], 目的是提供属性值的自动转换;
 * * 2.[SimpleTypeConverter], 可以为Spring BeanFactory外部的使用者, 去提供类型的转换, 也可以为依赖的注入提供帮助;
 *
 * @see PropertyEditor
 * @see BeanWrapper
 * @see BeanWrapperImpl
 * @see SimpleTypeConverter
 */
interface PropertyEditorRegistry {

    /**
     * 注册自定义的属性编辑器([PropertyEditor])到[PropertyEditorRegistry]当中
     *
     * @param requiredType 想要去编辑的类型
     * @param propertyEditor 编辑指定的类型(requiredType), 希望使用什么[PropertyEditor]去进行编辑?
     */
    fun registerCustomEditor(requiredType: Class<*>, propertyEditor: PropertyEditor)

    /**
     * 从[PropertyEditorRegistry]当中去寻找自定义的属性编辑器[PropertyEditor]
     *
     * @param requiredType 想要去编辑的类型
     * @return 如果针对想要编辑的类型(requiredType)找到了合适的[PropertyEditor], 那么return; 如果找不到, 那么直接return null
     */
    fun findCustomEditor(requiredType: Class<*>): PropertyEditor?
}