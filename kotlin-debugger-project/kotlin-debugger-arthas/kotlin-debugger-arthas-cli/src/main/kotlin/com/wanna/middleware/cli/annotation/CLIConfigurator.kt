package com.wanna.middleware.cli.annotation

import com.wanna.middleware.cli.*
import com.wanna.middleware.cli.converter.Converter
import com.wanna.middleware.cli.impl.ArgumentComparator
import com.wanna.middleware.cli.impl.DefaultCLI
import com.wanna.middleware.cli.impl.ReflectionUtils
import java.lang.reflect.Method
import javax.annotation.Nullable


/**
 * CLI的配置器的工具类, 负责提供工具方法, 去将一个beanClass当中的注解信息, 去解析成为CLI
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
object CLIConfigurator {

    /**
     * 根据给定的类当中的相关注解, 去解析成为[CLI]
     *
     * * 1.解析该类上的`@Name`注解成为命令的commandName;
     * * 2.解析该类当中的`@Option`注解和`@Argument`注解的Setter方法成为命令的相关参数.
     *
     * @param clazz clazz
     * @return CLI
     */
    @JvmStatic
    fun define(clazz: Class<*>): CLI {
        return define(clazz, false)
    }

    /**
     * 根据给定的类当中的相关注解, 去解析成为[CLI]
     *
     * * 1.解析该类上的`@Name`注解成为命令的commandName;
     * * 2.解析该类当中的`@Option`注解和`@Argument`注解的Setter方法成为命令的相关参数.
     *
     * @param clazz clazz
     * @param caseSensitive 是否区分大小写?
     * @return CLI
     */
    @JvmStatic
    fun define(clazz: Class<*>, caseSensitive: Boolean): CLI {
        val cli = DefaultCLI()
        cli.setCaseSensitive(caseSensitive)
        val summary = clazz.getAnnotation(Summary::class.java)
        val description = clazz.getAnnotation(Description::class.java)
        val hidden = clazz.getAnnotation(Hidden::class.java)

        // 必须要有@Name注解, 才能被定义成为一个CLI命令
        val name = clazz.getAnnotation(Name::class.java)
            ?: throw IllegalStateException("The command cannot be defined, the @Name annotation is missing.")

        if (name.value.isNotBlank()) {
            cli.setName(name.value)
        } else {
            throw IllegalStateException("The command cannot be defined, the @Name value is empty or blank")
        }
        if (summary != null) {
            cli.setSummary(summary.value)
        }
        if (description != null) {
            cli.setDescription(description.value)
        }
        if (hidden != null) {
            cli.setHidden(true)
        }

        // 解析所有Setter上的@Option注解和@Argument注解, 保存到CLI当中
        val setterMethods = ReflectionUtils.getSetterMethods(clazz)
        for (setterMethod in setterMethods) {
            val option = setterMethod.getAnnotation(Option::class.java)
            if (option != null) {
                cli.addOption(createOption(setterMethod))
            }
            val argument = setterMethod.getAnnotation(Argument::class.java)
            if (argument != null) {
                cli.addArgument(createArgument(setterMethod))
            }
        }

        // 对Arguments去进行排序
        cli.setArguments(cli.getArguments().sortedWith(ArgumentComparator))
        return cli
    }

    /**
     * 根据给定的Setter方法上标注的相关注解, 去构建出来Option对象
     *
     * @param method setter method
     * @return Option
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    private fun createOption(method: Method): com.wanna.middleware.cli.Option {
        val opt = TypedOption<Any>()
        val option = method.getAnnotation(Option::class.java)
            ?: throw IllegalStateException("Cannot find @Option annotation on method")

        opt.setArgName(option.argName)
            .setLongName(option.longName)
            .setShortName(option.shortName)
            .setRequired(option.required)

        val description = method.getAnnotation(Description::class.java)
        if (description != null) {
            opt.setDescription(description.value)
        }
        val hidden = method.getAnnotation(Hidden::class.java)
        if (hidden != null) {
            opt.setHidden(true)
        }

        if (ReflectionUtils.isMultiple(method)) {
            opt.setType(ReflectionUtils.getComponentType(method, 0) as Class<Any>).setMultipleValued(true)
        } else {
            val type = method.parameterTypes[0]
            opt.setType(type as Class<Any>)
            if (type != Boolean::class.java && type != Boolean::class.javaObjectType) {
                opt.setSingleValued(true)
            }
        }

        val convertedBy = method.getAnnotation(ConvertedBy::class.java)
        if (convertedBy != null) {
            opt.setConverter(ReflectionUtils.newInstance(convertedBy.value.java) as Converter<Any>)
        }

        val defaultValue = method.getAnnotation(DefaultValue::class.java)
        if (defaultValue != null) {
            opt.setDefaultValue(defaultValue.value)
        }

        val parsedAsList = method.getAnnotation(ParsedAsList::class.java)
        if (parsedAsList != null) {
            opt.setParsedAsList(true).setListSeparator(parsedAsList.separator)
        }

        return opt
    }

    /**
     * 根据给定的Setter方法上标注的注解, 去构建出来Argument对象
     *
     * @param method method
     * @return Argument
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    private fun createArgument(method: Method): com.wanna.middleware.cli.Argument {
        val arg = TypedArgument<Any>()
        val argument = method.getAnnotation(Argument::class.java)
            ?: throw IllegalStateException("Cannot find @Argument annotation on method")
        arg.setIndex(argument.index)
        arg.setArgName(argument.argName)
        arg.setRequired(argument.required)

        val description = method.getAnnotation(Description::class.java)
        if (description != null) {
            arg.setDescription(description.value)
        }
        val hidden = method.getAnnotation(Hidden::class.java)
        if (hidden != null) {
            arg.setHidden(true)
        }

        if (ReflectionUtils.isMultiple(method)) {
            arg.setType(ReflectionUtils.getComponentType(method, 0) as Class<Any>)
        } else {
            val type = method.parameterTypes[0]
            arg.setType(type as Class<Any>)
        }

        val convertedBy = method.getAnnotation(ConvertedBy::class.java)
        if (convertedBy != null) {
            arg.setConverter(ReflectionUtils.newInstance(convertedBy.value.java) as Converter<Any>)
        }

        val defaultValue = method.getAnnotation(DefaultValue::class.java)
        if (defaultValue != null) {
            arg.setDefaultValue(defaultValue.value)
        }

        return arg
    }

    /**
     * 执行CLI的注入, 对于`@Option`/`@Argument`注解的Setter方法去进行回调, 完成注入
     *
     * @param commandLine commandLine
     * @param obj 要去进行注入的实例对象
     */
    @JvmStatic
    fun inject(commandLine: CommandLine, obj: Any) {
        val setterMethods = ReflectionUtils.getSetterMethods(obj.javaClass)
        for (setterMethod in setterMethods) {

            // check @Option
            val option = setterMethod.getAnnotation(Option::class.java)
            if (option != null) {
                var name = option.longName
                if (name.isBlank()) {
                    name = option.shortName
                }
                try {
                    val injected = getOptionValue(setterMethod, name, commandLine)
                    if (injected != null) {
                        setterMethod.isAccessible = true
                        setterMethod.invoke(obj, injected)
                    }
                } catch (ex: Exception) {
                    throw CLIException("Cannot inject value for option '$name'", ex)
                }
            }

            // check @Argument
            val argument = setterMethod.getAnnotation(Argument::class.java)
            if (argument != null) {
                val index = argument.index
                try {
                    val injected = getArgumentValue(setterMethod, index, commandLine)
                    if (injected != null) {
                        setterMethod.isAccessible = true
                        setterMethod.invoke(obj, injected)
                    }
                } catch (ex: Exception) {
                    throw CLIException("Cannot inject value for argument '$index'", ex)
                }
            }
        }
    }

    @Nullable
    @JvmStatic
    private fun getArgumentValue(method: Method, index: Int, commandLine: CommandLine): Any? {
        return Any()
    }

    @Nullable
    @JvmStatic
    private fun getOptionValue(method: Method, name: String, commandLine: CommandLine): Any? {
        return Any()
    }
}