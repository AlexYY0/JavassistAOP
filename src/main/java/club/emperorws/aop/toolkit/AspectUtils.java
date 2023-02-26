package club.emperorws.aop.toolkit;

import club.emperorws.aop.annotation.*;
import club.emperorws.aop.constant.Constants;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.*;

/**
 * {@link club.emperorws.aop.DoAspect}DoAspect代码过长的--->方法封装
 *
 * @author: EmperorWS
 * @date: 2023/2/22 19:54
 * @description: AspectUtils: 切面
 */
public class AspectUtils {

    private AspectUtils() {
        throw new IllegalStateException("Utility AspectUtils class can not use constructor.");
    }

    /**
     * 复制方法并添加复制的方法到原class中
     *
     * @param clazz            复制方法的原class，复制后的方法也添加到该class
     * @param method           被复制的方法
     * @param newMethodName    复制后的方法名称
     * @param isCopyAnnotation 是否复制方法的注解
     * @return 复制的方法
     * @throws CannotCompileException 异常
     */
    public static CtMethod copyAndAddMethod(CtClass clazz, CtMethod method, String newMethodName, boolean isCopyAnnotation) throws CannotCompileException {
        //1. 复制方法
        CtMethod srcMethod = CtNewMethod.copy(method, clazz, null);
        //2. 给新的方法换一个名字
        srcMethod.setName(newMethodName);
        //3. 复制运行时可见注解
        if (isCopyAnnotation) {
            srcMethod.getMethodInfo().addAttribute(method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag));
        }
        //4. 将这个方法添加到类里面
        clazz.addMethod(srcMethod);
        return srcMethod;
    }

    /**
     * 获取切面编程方法
     *
     * @param aspectClass 切面class类
     * @return 切面编程方法
     * @throws ClassNotFoundException 异常
     */
    public static Map<Class<?>, CtMethod> makeAspectOrder(CtClass aspectClass) throws ClassNotFoundException {
        //1. 获取切面的所有方法，并遍历切面的所有方法，找出对应的切面编程
        CtMethod[] aspectMethods = aspectClass.getDeclaredMethods();
        //2. 按照顺序执行切面
        Map<Class<?>, CtMethod> methodMap = new HashMap<>(5);
        for (CtMethod aspectMethod : aspectMethods) {
            //前置通知
            Object before = aspectMethod.getAnnotation(Before.class);
            if (Objects.nonNull(before)) {
                methodMap.put(Before.class, aspectMethod);
            }
            //返回通知
            Object afterReturning = aspectMethod.getAnnotation(AfterReturning.class);
            if (Objects.nonNull(afterReturning)) {
                methodMap.put(AfterReturning.class, aspectMethod);
            }
            //后置通知
            Object after = aspectMethod.getAnnotation(After.class);
            if (Objects.nonNull(after)) {
                methodMap.put(After.class, aspectMethod);
            }
            //异常通知
            Object afterThrowing = aspectMethod.getAnnotation(AfterThrowing.class);
            if (Objects.nonNull(afterThrowing)) {
                methodMap.put(AfterThrowing.class, aspectMethod);
            }
            //环绕通知
            Object around = aspectMethod.getAnnotation(Around.class);
            if (Objects.nonNull(around)) {
                methodMap.put(Around.class, aspectMethod);
            }
        }
        return methodMap;
    }

    /**
     * 创建局部变量
     *
     * @param aspectClass 切面class
     * @param method      切点方法
     */
    public static void createLocalVariables(CtClass aspectClass, CtMethod method) throws CannotCompileException {
        CtClass paramPoint = null, paramAspectClazz = null;
        try {
            paramPoint = Constants.POOL.getOrNull("club.emperorws.aop.entity.Pointcut");
            method.addLocalVariable("point", paramPoint);
            paramAspectClazz = Constants.POOL.getOrNull(aspectClass.getName());
            method.addLocalVariable("aspectObj", paramAspectClazz);
        } finally {
            if (Objects.nonNull(paramPoint)) {
                paramPoint.detach();
            }
            if (Objects.nonNull(paramAspectClazz)) {
                paramAspectClazz.detach();
            }
        }
    }

    /**
     * 为之前创建的局部变量赋值
     *
     * @param method      切点方法
     * @param aspectClass 切面方法
     */
    public static void assignLocalVariables(CtMethod method, CtClass aspectClass) throws CannotCompileException {
        String paramCode = "" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                "point = new club.emperorws.aop.entity.Pointcut(method,$args);" +
                "aspectObj = new " + aspectClass.getName() + "();";
        method.insertBefore(paramCode);
    }

    /**
     * 前置通知字节码增强
     *
     * @param method      方法
     * @param aspectClass 切面class
     * @param methodMap   用于获取{@link Before}标注的方法
     * @throws CannotCompileException 异常
     */
    public static void aspectBefore(CtMethod method, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws CannotCompileException {
        CtMethod aspectMethod = methodMap.get(Before.class);
        if (Objects.isNull(aspectMethod)) {
            //前置局部变量赋值
            assignLocalVariables(method, aspectClass);
            return;
        }
        String beforeCode = "" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                "point = new club.emperorws.aop.entity.Pointcut(method,$args);" +
                "aspectObj = new " + aspectClass.getName() + "();" +
                "aspectObj." + aspectMethod.getName() + "(point);";
        method.insertBefore(beforeCode);
    }

    /**
     * 返回通知字节码增强
     *
     * @param method      方法
     * @param aspectClass 切面class
     * @param methodMap   用于获取{@link AfterReturning}AfterReturning标注的方法
     * @throws CannotCompileException 异常
     */
    public static void aspectAfterReturning(CtMethod method, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws CannotCompileException {
        CtMethod aspectMethod = methodMap.get(AfterReturning.class);
        if (Objects.isNull(aspectMethod)) {
            return;
        }
        String afterReturningCode = "" +
                "point.setReturnValue($_);" +
                "aspectObj." + aspectMethod.getName() + "(point);";
        method.insertAfter(afterReturningCode);
    }

    /**
     * 异常通知字节码增强
     *
     * @param method      方法
     * @param aspectClass 切面class
     * @param methodMap   用于获取{@link AfterThrowing}AfterThrowing标注的方法
     * @throws CannotCompileException 异常
     * @throws NotFoundException      异常
     */
    public static void aspectAfterThrowing(CtMethod method, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws CannotCompileException, NotFoundException {
        CtMethod aspectMethod = methodMap.get(AfterThrowing.class);
        if (Objects.isNull(aspectMethod)) {
            return;
        }
        String exceptionCode = "" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(method,$args).setE($e);" +
                aspectClass.getName() + " aspectObj = new " + aspectClass.getName() + "();" +
                "if(point.getReturnValue() != null){" +
                "return (" + method.getReturnType().getName() + ")point.getReturnValue();" +
                "}" +
                "throw $e;";
        CtClass exceptCtClass = null;
        try {
            exceptCtClass = Constants.POOL.get("java.lang.Exception");
            method.addCatch(exceptionCode, exceptCtClass);
        } finally {
            if (Objects.nonNull(exceptCtClass)) {
                exceptCtClass.detach();
            }
        }
    }

    /**
     * 后置通知字节码增强
     *
     * @param method      方法
     * @param aspectClass 切面class
     * @param methodMap   用于获取{@link After}After标注的方法
     * @throws CannotCompileException 异常
     */
    public static void aspectAfter(CtMethod method, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws CannotCompileException {
        CtMethod aspectMethod = methodMap.get(After.class);
        if (Objects.isNull(aspectMethod)) {
            return;
        }
        String afterCode = "" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(method,$args);" +
                aspectClass.getName() + " aspectObj = new " + aspectClass.getName() + "();" +
                "aspectObj." + aspectMethod.getName() + "(point);";
        method.insertAfter(afterCode, true);
    }

    /**
     * 环绕通知字节码增强
     *
     * @param clazz          切点类
     * @param method         切点方法
     * @param methodIsStatic 方法是否是静态方法
     * @param aspectClass    切面class
     * @param methodMap      用于获取{@link Around}Around标志的方法
     * @throws NotFoundException      异常
     * @throws CannotCompileException 异常
     */
    public static void aspectAround(CtClass clazz, CtMethod method, boolean methodIsStatic, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws NotFoundException, CannotCompileException {
        CtMethod aspectMethod = methodMap.get(Around.class);
        if (Objects.isNull(aspectMethod)) {
            return;
        }
        //1. 先复制原方法，由于嵌套执行
        String srcMethodName = method.getName() + "_" + UUID.randomUUID().toString().replace("-", "");
        copyAndAddMethod(clazz, method, srcMethodName, true);
        //2. 再嵌套设置新方法
        StringBuilder aroundCode = new StringBuilder("{" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + srcMethodName + "\",$sig);" +
                "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(method,$args).setThisProceedObj(" + (methodIsStatic ? "null" : "$0") + ");" +
                aspectClass.getName() + " aspectObj = new " + aspectClass.getName() + "();" +
                "aspectObj." + aspectMethod.getName() + "(point);");
        //非void的方法，增加return返回值
        if (!method.getReturnType().equals(CtClass.voidType)) {
            aroundCode.append("return ($r)point.getReturnValue();");
        }
        aroundCode.append("}");
        method.setBody(aroundCode.toString());
    }
}
