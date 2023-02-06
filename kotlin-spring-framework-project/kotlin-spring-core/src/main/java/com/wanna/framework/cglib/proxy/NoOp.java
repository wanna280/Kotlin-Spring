package com.wanna.framework.cglib.proxy;


/**
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/7
 */
public interface NoOp extends Callback
{
    /**
     * A thread-safe singleton instance of the <code>NoOp</code> callback.
     */
    NoOp INSTANCE = new NoOp() { };
}
