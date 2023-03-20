package com.hmdp.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author xjh
 * @create 2023-03-17 23:46
 */
@Component
@Aspect
@Slf4j
public class LogAspect {

    //@Pointcut("execution(* com.hmdp.service.impl.*.*(..))")
    //public void pointcut() {
    //}

    //@Before("pointcut()")
    //public void before(JoinPoint joinPoint) {
    //    Object aThis = joinPoint.getThis();
    //    log.info("正在执行的对象：{}",aThis);
    //}
}