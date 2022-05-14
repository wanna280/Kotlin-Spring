package com.wanna.framework.beans.factory.support

/**
 * 它是一个DisposableBean，它拥有destroy方法，可以设置在对Bean去进行预销毁(before-destruction)时，应该执行的相关收尾工作；
 * 它的作用和JDK当中提供的AutoCloseable相同，都是提供一个回调方法去完成destroy的收尾工作；
 *
 * @see AutoCloseable
 */
interface DisposableBean {
    fun destroy()
}