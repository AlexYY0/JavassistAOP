package club.emperorws.aop.preload.annotation;

/**
 * 捕获异常，统一处理返回的注解
 *
 * @author: EmperorWS
 * @date: 2023/2/21 22:51
 * @description: CatchException: 捕获异常，统一处理返回的注解
 */
public @interface CatchException {

    /**
     * 需要捕获的异常，统一处理的异常
     *
     * @return 需要捕获的异常，统一处理的异常
     */
    Class<? extends Exception> exceptionClass() default Exception.class;

    /**
     * 出现异常后，返回的提示信息
     *
     * @return 出现异常后，返回的提示信息
     */
    String errorMessage() default "接口异常，请联系管理员！";
}
