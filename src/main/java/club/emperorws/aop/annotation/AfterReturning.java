package club.emperorws.aop.annotation;

import java.lang.annotation.*;

/**
 * 返回通知
 *
 * @author: EmperorWS
 * @date: 2023/2/18 15:22
 * @description: AfterReturning: 返回通知
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterReturning {
}
