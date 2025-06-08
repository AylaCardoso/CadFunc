package com.empresa.util;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Logger;

@Interceptor
public class LoggingInterceptor {
    private static final Logger LOGGER = Logger.getLogger(LoggingInterceptor.class.getName());

    @AroundInvoke
    public Object logMethod(InvocationContext context) throws Exception {
        LOGGER.info("Método " + context.getMethod().getName() + " chamado");
        return context.proceed();
    }
}
