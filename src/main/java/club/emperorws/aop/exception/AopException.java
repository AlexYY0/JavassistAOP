package club.emperorws.aop.exception;

/**
 * Aop处理异常
 *
 * @author: EmperorWS
 * @date: 2023/8/6 11:54
 * @description: AopException: Aop处理异常
 */
public class AopException extends RuntimeException {

    public AopException() {
        super();
    }

    public AopException(String message) {
        super(message);
    }

    public AopException(String message, Throwable cause) {
        super(message, cause);
    }

    public AopException(Throwable cause) {
        super(cause);
    }
}
