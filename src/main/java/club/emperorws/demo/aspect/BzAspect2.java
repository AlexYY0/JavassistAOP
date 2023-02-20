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
@Aspect(order = 1, pointcutAnnotationClassPath = "club.emperorws.demo.aspect.annotation.CatchException2")
public class BzAspect2 {

    @Before
    public void before(Pointcut pointcut) {
        Console.log("before2:{}", pointcut);
    }

    @AfterReturning
    public void afterReturning(Pointcut pointcut) {
        Console.log("afterReturning2:{}", pointcut);
    }

    @AfterThrowing
    public void afterThrowing(Pointcut pointcut) {
        Console.log("afterThrowing2:{}", pointcut);
    }

    @After
    public void after(Pointcut pointcut) {
        Console.log("after2:{}", pointcut);
    }

    @Around
    public void around(Pointcut pointcut) throws Throwable {
        Console.log("around2-start:{}", pointcut);
        Object result = pointcut.proceed();
        Console.log("around2-end:{}", result);
    }
}
