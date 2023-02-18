package club.emperorws.aop.annotation;

import java.lang.annotation.*;

/**
 * 异常通知
 *
 * @author: EmperorWS
 * @date: 2023/2/17 21:26
 * @description: AfterThrowing: 异常通知
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterThrowing {
}
