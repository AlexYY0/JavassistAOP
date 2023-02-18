package club.emperorws.demo.aspect;

import club.emperorws.aop.annotation.*;
import club.emperorws.aop.entity.Pointcut;
import cn.hutool.core.lang.Console;

/**
 * 业务切面编程
 *
 * @author: EmperorWS
 * @date: 2023/2/17 0:41
 * @description: BzAspect: 业务切面编程
 */
@Aspect(order = 0, pointcutAnnotationClassPath = "club.emperorws.demo.aspect.annotation.CatchException")
public class BzAspect {

    @Before
    public void before(Pointcut pointcut) {
        Console.log("before");
        Console.log(pointcut);
    }

    @AfterReturning
    public void afterReturning(Pointcut pointcut) {
        Console.log("afterReturning");
        Console.log(pointcut);
    }

    @AfterThrowing
    public void afterThrowing(Pointcut pointcut) {
        Console.log("afterThrowing");
        Console.log(pointcut);
    }

    @After
    public void after(Pointcut pointcut) {
        Console.log("after");
        Console.log(pointcut);
    }

    @Around
    public void around(Pointcut pointcut) throws Throwable {
        Console.log("around");
        Object result = pointcut.proceed();
        Console.log(pointcut);
        Console.log("result:{}", result);
    }
}
