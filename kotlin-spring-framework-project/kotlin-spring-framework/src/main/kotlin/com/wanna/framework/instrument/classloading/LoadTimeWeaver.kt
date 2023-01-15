package com.wanna.framework.instrument.classloading

import java.lang.instrument.ClassFileTransformer

/**
 * 类加载时编织的接口
 */
interface LoadTimeWeaver {

    /**
     * 添加一个ClassFileTransformer给LoadTimeWeaver
     *
     * @param transformer 想要添加的Class文件转换器
     */
    fun addTransformer(transformer: ClassFileTransformer)

    /**
     * 获取Instrument的类加载器, 用来去支持用户自定义的Transformer, 实现AspectJ风格的加载时编制
     *
     * @return 根据注册的Transformer, 暴露那些给被Instrumented的类
     */
    fun getInstrumentableClassLoader() : ClassLoader

    /**
     * 返回一个临时使用的类加载器, 这个类加载器不要和getInstrumentableClassLoader返回的相同;
     * 主要用于在不影响parent类加载器的情况下去开启类的加载和检测
     *
     * @return 临时使用的ClassLoader, 尽可能每次都创建一个新的ClassLoader对象
     */
    fun getThrowawayClassLoader() : ClassLoader
}