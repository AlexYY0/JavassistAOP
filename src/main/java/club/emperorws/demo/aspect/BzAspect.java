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

    @AfterThrowing
    public void afterThrowing(Pointcut pointcut) {
        Console.log("afterThrowing1:{}", pointcut);
    }

    @After
    public void after(Pointcut pointcut) {
        Console.log("after1:{}", pointcut);
    }

    @Around
    public void around(Pointcut pointcut) throws Throwable {
        Console.log("around1-start:{}", pointcut);
        pointcut.proceed();
        Console.log("around1-end:{}", pointcut.getReturnValue());
    }

    @AfterReturning
    public void afterReturning(Pointcut pointcut) {
        Console.log("afterReturning1:{}", pointcut);
    }

    @Before
    public void before(Pointcut pointcut) {
        Console.log("before1:{}", pointcut);
    }
}
