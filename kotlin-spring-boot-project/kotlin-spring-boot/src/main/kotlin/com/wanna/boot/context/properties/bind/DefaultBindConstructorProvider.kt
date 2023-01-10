package com.wanna.boot.context.properties.bind

import com.wanna.framework.lang.Nullable
import java.lang.reflect.Constructor

/**
 * [BindConstructorProvider]的默认实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 */
class DefaultBindConstructorProvider : BindConstructorProvider {

    @Nullable
    override fun getBindConstructor(bindable: Bindable<*>, isNestedConstructorBinding: Boolean): Constructor<*>? {
        return getBindConstructor(bindable.type.resolve(), isNestedConstructorBinding)
    }

    @Nullable
    private fun getBindConstructor(type: Class<*>?, isNestedConstructorBinding: Boolean): Constructor<*>? {
        type ?: return null
        val constructors = Constructors.getConstructors(type)
        if (constructors.bind != null && isNestedConstructorBinding) {
            if (constructors.hasAutowired) {
                throw IllegalStateException("${type.name} declares @ConstructorBinding and @Autowired constructor")
            }
        }
        return constructors.bind
    }

    class Constructors(val hasAutowired: Boolean, val bind: Constructor<*>?) {

        companion object {

            @JvmStatic
            fun getConstructors(type: Class<*>): Constructors {
                // TODO
                return Constructors(false, null)
            }
        }
    }
}