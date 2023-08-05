# JavassistAOP
使用Javassist实现的AOP框架，通过修改字节码的方式实现切面编程，适用于普通Java项目。

##### 有五大注解：@Around、@Before、@AfterReturning、@AfterThrowing、@After（可以在Around或AfterReturning中获取切点的返回值）

##### 执行顺序分别为：
1. Around->Before->AfterReturning->After
2. Around->Before->AfterThrowing->After

> 注:
> 1. 可以在Around或AfterReturning中获取切点的返回值；
> 2. 切面注解的执行顺序（优先级order），从0开始，数值越小，优先级越高；
> 3. 执行切面的方法，方法参数有且只能有一个，为club.emperorws.aop.entity.Pointcut类型的参数；
> 4. 尽量少使用Around，因为Around使用反射实现，使用Before + AfterReturning = Around代替为最佳效果；
> 5. @Aspect注解的pointcut参数，支持execution(正则匹配)和@annotation(全词匹配)，例：@Aspect(order = 1, pointcut = "execution(\"* club.emperorws.aop.demo.controller.Business*.\*(*)\") && @annotation(\"club.emperorws.aop.demo.aspect.annotation.CatchException2\")")。

##### 代码示例

###### 1. 切面注解

```java
package club.emperorws.aop.demo.aspect.annotation;

import java.lang.annotation.*;

/**
 * 捕获异常的注解
 *
 * @author: EmperorWS
 * @date: 2023/2/17 0:51
 * @description: CatchException: 捕获异常的注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CatchException {
}
```

###### 2. 切面方法

```java
package club.emperorws.aop.demo.aspect;

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
@Aspect(order = 1, pointcut = "execution(\"* club.emperorws.aop.demo.controller.Business*.*(*)\") && @annotation(\"club.emperorws.aop.demo.aspect.annotation.CatchException2\")")
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
        Object result = pointcut.proceed();
        Console.log("around1-end:{}", result);
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
```
###### 3. 使用切面注解

```java
package club.emperorws.aop.demo;

import club.emperorws.aop.demo.aspect.annotation.CatchException;
import club.emperorws.aop.entity.R;

/**
 * 业务方法接口
 *
 * @author: EmperorWS
 * @date: 2023/2/17 0:29
 * @description: BusinessController: 业务方法接口
 */
public class BusinessController {

    @CatchException
    public R doSth(String aa, Integer bb) throws Exception {
        System.out.println("Start do sth");
        int a = 0, b = 1, c = 2;
        int r = a + b * c;
        //int e = 1 / 0;
        System.out.println("End do sth");
        return new R("0", r);
    }
}
```
