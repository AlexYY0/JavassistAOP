package club.emperorws.aop.annotation;

import java.lang.annotation.*;

/**
 * 后置通知
 *
 * @author: EmperorWS
 * @date: 2023/2/17 21:11
 * @description: After: 后置通知
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface After {
}
