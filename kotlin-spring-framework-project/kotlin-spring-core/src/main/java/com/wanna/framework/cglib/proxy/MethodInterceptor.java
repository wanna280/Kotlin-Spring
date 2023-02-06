package com.wanna.framework.cglib.proxy;



/**
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/7
 */
public interface MethodInterceptor extends Callback {
    /**
     * All generated proxied methods call this method instead of the original method.
     * The original method may either be invoked by normal reflection using the Method object,
     * or by using the MethodProxy (faster).
     *
     * @param obj    "this", the enhanced object
     * @param method intercepted Method
     * @param args   argument array; primitive types are wrapped
     * @param proxy  used to invoke super (non-intercepted method); may be called
     *               as many times as needed
     * @return any value compatible with the signature of the proxied method. Method returning void will ignore this value.
     *
     * @throws Throwable any exception may be thrown; if so, super method will not be invoked
     * @see net.sf.cglib.proxy.MethodProxy
     */
    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                            MethodProxy proxy) throws Throwable;

}
