package club.emperorws.demo.aspect.annotation;

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
