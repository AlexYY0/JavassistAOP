package club.emperorws.aop.annotation;

import java.lang.annotation.*;

/**
 * 环绕通知
 *
 * @author: EmperorWS
 * @date: 2023/2/17 21:26
 * @description: Around: 环绕通知
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Around {
}
