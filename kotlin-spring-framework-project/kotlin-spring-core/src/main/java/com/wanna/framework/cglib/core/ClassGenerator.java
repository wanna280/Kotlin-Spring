package com.wanna.framework.cglib.core;


import com.wanna.framework.asm.ClassVisitor;

/**
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/7
 */
public interface ClassGenerator {
    void generateClass(ClassVisitor v) throws Exception;
}
