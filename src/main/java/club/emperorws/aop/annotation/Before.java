package club.emperorws.aop.annotation;

import java.lang.annotation.*;

/**
 * 前置通知
 *
 * @author: EmperorWS
 * @date: 2023/2/17 21:26
 * @description: Before: 前置通知
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Before {
}
