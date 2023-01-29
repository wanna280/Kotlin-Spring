package com.wanna.framework.beans.factory.xml

import com.wanna.framework.lang.Nullable

/**
 * NamespaceHandler的解析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
fun interface NamespaceHandlerResolver {

    /**
     * 根据NamespaceUri去解析到处理该Namespace的Handler
     *
     * @param namespaceUri namespaceUri
     * @return NamespaceHandler(如果找不到处理该NamespaceUri的Handler, 那么return null)
     */
    @Nullable
    fun resolve(namespaceUri: String): NamespaceHandler?
}