package com.aghayev.ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(com.aghayev.ecommerce.config.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.info(
                    "method={} status=SUCCESS executionTimeMs={}",
                    joinPoint.getSignature().toShortString(),
                    executionTimeMs
            );
            return result;
        } catch (Throwable throwable) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error(
                    "method={} status=FAILED executionTimeMs={} error={}",
                    joinPoint.getSignature().toShortString(),
                    executionTimeMs,
                    throwable.getClass().getSimpleName()
            );
            throw throwable;
        }
    }
}
