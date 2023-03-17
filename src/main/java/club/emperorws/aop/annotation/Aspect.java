package club.emperorws.aop.annotation;

import java.lang.annotation.*;

/**
 * 切面编程，标志着这是一个切面编程
 *
 * @author: EmperorWS
 * @date: 2023/2/17 21:27
 * @description: Aspect: 切面编程，标志着这是一个切面编程
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Aspect {

    /**
     * 切面注解的执行顺序（优先级）。从0开始
     * 数值越小，优先级越高
     *
     * @return 切面注解的执行顺序（优先级）
     * @return 切面注解的执行顺序（优先级）
     */
    int order();

    /**
     * 切面的使用范围：切点表达式
     * 支持execution(正则匹配)和@annotation(全词匹配)
     *
     * @return 切点表达式
     */
    String pointcut();
}
