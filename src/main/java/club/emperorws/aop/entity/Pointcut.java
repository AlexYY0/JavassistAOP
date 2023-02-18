package club.emperorws.aop.entity;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * 存储切点的一些信息
 *
 * @author: EmperorWS
 * @date: 2023/2/17 21:44
 * @description: Point: 存储切点的一些信息
 */
public class Pointcut {

    /**
     * 被切点的对象
     */
    private Object thisProceedObj;

    /**
     * 切点方法的信息
     */
    private Method method;

    /**
     * 切点方法的请求参数
     */
    private Object[] args;

    /**
     * 异常
     */
    private Throwable e;

    /**
     * 切点的返回结果
     */
    private Object returnValue;

    public Pointcut() {
    }

    public Pointcut(Object thisProceedObj, Method method, Object[] args) {
        this.thisProceedObj = thisProceedObj;
        this.method = method;
        this.args = args;
    }

    public Pointcut(Object thisProceedObj, Method method, Object[] args, Object returnValue) {
        this.thisProceedObj = thisProceedObj;
        this.method = method;
        this.args = args;
        this.returnValue = returnValue;
    }

    public Pointcut(Object thisProceedObj, Method method, Object[] args, Throwable e) {
        this.thisProceedObj = thisProceedObj;
        this.method = method;
        this.args = args;
        this.e = e;
    }

    public Object getThisProceedObj() {
        return thisProceedObj;
    }

    public void setThisProceedObj(Object thisProceedObj) {
        this.thisProceedObj = thisProceedObj;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Throwable getE() {
        return e;
    }

    public void setE(Throwable e) {
        this.e = e;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * 反射执行切点方法
     *
     * @return 切点方法的返回结果
     * @throws Throwable 异常
     */
    public Object proceed() throws Throwable {
        //如果是静态方法
        if (Modifier.isStatic(method.getModifiers())) {
            this.returnValue = method.invoke(null, args);
        } else {
            this.returnValue = method.invoke(thisProceedObj, args);
        }
        return returnValue;
    }

    @Override
    public String toString() {
        return "Pointcut{" +
                "thisProceedObj=" + thisProceedObj +
                ", method=" + method +
                ", args=" + Arrays.toString(args) +
                ", e=" + e +
                ", returnValue=" + returnValue +
                '}';
    }
}
