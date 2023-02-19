# JavassistAOP
使用Javassist实现的AOP框架，通过改变字节码的方式实现切面编程，适用于普通Java项目。

有五大注解：
Around->Before->AfterReturning->After
Around->Before->AfterThrowing->After

可以在Around或AfterReturning中获取切点的返回值

> 注:尽量减少使用Around，使用Before+AfterReturning或Before+After替代Around，因为Around使用了反射，性能不佳
