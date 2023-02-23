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
     * @param method      切点方法
     */
    public static void createLocalVariables(CtMethod method) throws CannotCompileException {
        CtClass paramPoint = null;
        try {
            paramPoint = Constants.POOL.getOrNull("club.emperorws.aop.entity.Pointcut");
            method.addLocalVariable("point", paramPoint);
        } finally {
            if (Objects.nonNull(paramPoint)) {
                paramPoint.detach();
            }
        }
    }

    /**
     * 为之前创建的局部变量赋值
     *
     * @param method 切点方法
     */
    public static void assignLocalVariables(CtMethod method) throws CannotCompileException {
        String paramCode = "" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                "point = new club.emperorws.aop.entity.Pointcut(method,$args);";
        method.insertBefore(paramCode);
    }

    /**
     * 前置通知字节码增强
     *
     * @param clazz       切点类
     * @param method      方法
     * @param aspectClass 切面class
     * @param methodMap   用于获取{@link Before}标注的方法
     * @throws CannotCompileException 异常
     */
    public static void aspectBefore(CtClass clazz, CtMethod method, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws CannotCompileException {
        CtMethod aspectMethod = methodMap.get(Before.class);
        if (Objects.isNull(aspectMethod)) {
            //前置局部变量赋值
            assignLocalVariables(method);
            return;
        }
        //1. 复制切面方法到切点Class
        String srcAspectMethodName = method.getName() + "_" + aspectClass.getSimpleName() + "$" + aspectMethod.getName();
        copyAndAddMethod(clazz, aspectMethod, srcAspectMethodName, false);
        //2. 插入方法
        String beforeCode = "" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                "point = new club.emperorws.aop.entity.Pointcut(method,$args);" +
                srcAspectMethodName + "(point);";
        method.insertBefore(beforeCode);
    }

    /**
     * 返回通知字节码增强
     *
     * @param clazz       切点类
     * @param method      方法
     * @param aspectClass 切面class
     * @param methodMap   用于获取{@link AfterReturning}AfterReturning标注的方法
     * @throws CannotCompileException 异常
     */
    public static void aspectAfterReturning(CtClass clazz, CtMethod method, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws CannotCompileException {
        CtMethod aspectMethod = methodMap.get(AfterReturning.class);
        if (Objects.isNull(aspectMethod)) {
            return;
        }
        //1. 复制切面方法到切点Class
        String srcAspectMethodName = method.getName() + "_" + aspectClass.getSimpleName() + "$" + aspectMethod.getName();
        copyAndAddMethod(clazz, aspectMethod, srcAspectMethodName, false);
        //2. 插入方法
        String afterReturningCode = "" +
                "point.setReturnValue($_);" +
                srcAspectMethodName + "(point);";
        method.insertAfter(afterReturningCode);
    }

    /**
     * 异常通知字节码增强
     *
     * @param clazz       切点类
     * @param method      方法
     * @param aspectClass 切面class
     * @param methodMap   用于获取{@link AfterThrowing}AfterThrowing标注的方法
     * @throws CannotCompileException 异常
     * @throws NotFoundException      异常
     */
    public static void aspectAfterThrowing(CtClass clazz, CtMethod method, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws CannotCompileException, NotFoundException {
        CtMethod aspectMethod = methodMap.get(AfterThrowing.class);
        if (Objects.isNull(aspectMethod)) {
            return;
        }
        //1. 复制切面方法到切点Class
        String srcAspectMethodName = method.getName() + "_" + aspectClass.getSimpleName() + "$" + aspectMethod.getName();
        copyAndAddMethod(clazz, aspectMethod, srcAspectMethodName, false);
        //2. 插入方法
        String exceptionCode = "" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(method,$args).setE($e);" +
                srcAspectMethodName + "(point);" +
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
     * @param clazz       切点类
     * @param method      方法
     * @param aspectClass 切面class
     * @param methodMap   用于获取{@link After}After标注的方法
     * @throws CannotCompileException 异常
     */
    public static void aspectAfter(CtClass clazz, CtMethod method, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws CannotCompileException {
        CtMethod aspectMethod = methodMap.get(After.class);
        if (Objects.isNull(aspectMethod)) {
            return;
        }
        //1. 复制切面方法到切点Class
        String srcAspectMethodName = method.getName() + "_" + aspectClass.getSimpleName() + "$" + aspectMethod.getName();
        copyAndAddMethod(clazz, aspectMethod, srcAspectMethodName, false);
        //2. 插入方法
        String afterCode = "" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(method,$args);" +
                srcAspectMethodName + "(point);";
        method.insertAfter(afterCode, true);
    }

    /**
     * 环绕通知字节码增强
     *
     * @param clazz       切点类
     * @param method      切点方法
     * @param aspectClass 切面class
     * @param methodMap   用于获取{@link Around}Around标志的方法
     * @throws NotFoundException      异常
     * @throws CannotCompileException 异常
     */
    public static void aspectAround(CtClass clazz, CtMethod method, CtClass aspectClass, Map<Class<?>, CtMethod> methodMap) throws NotFoundException, CannotCompileException {
        CtMethod aspectMethod = methodMap.get(Around.class);
        if (Objects.isNull(aspectMethod)) {
            return;
        }
        //1. 获取原方法的请求参数类型，方便后续的处理
        CtClass[] parameterTypes = method.getParameterTypes();
        StringJoiner parameterTypeSj = new StringJoiner(",", "(", ");");
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypeSj.add("(" + parameterTypes[i].getName() + ")$0.getArgs()[" + i + "]");
        }
        //2. 先复制原方法，由于嵌套执行
        String srcMethodName = method.getName() + "_" + UUID.randomUUID().toString().replace("-", "");
        copyAndAddMethod(clazz, method, srcMethodName, true);
        //3. 复制切面方法到切点Class
        String srcAspectMethodName = method.getName() + "_" + aspectClass.getSimpleName() + "$" + aspectMethod.getName();
        CtMethod srcAspectMethod = copyAndAddMethod(clazz, aspectMethod, srcAspectMethodName, false);
        //4. 修改aspect切面@Around方法里的pointcut.proceed()为真正的切点方法
        srcAspectMethod.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getClassName().equals("club.emperorws.aop.entity.Pointcut") && m.getMethodName().equals("proceed")) {
                    m.replace("$_ = " + srcMethodName + parameterTypeSj +
                            "$0.setReturnValue($_);");
                }
            }
        });
        //4. 再修改原切点方法体
        StringBuilder aroundCode = new StringBuilder("{" +
                "java.lang.reflect.Method method = $class.getDeclaredMethod(\"" + srcMethodName + "\",$sig);" +
                "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(method,$args);" +
                srcAspectMethodName + "(point);");
        //非void的方法，增加return返回值
        if (!method.getReturnType().equals(CtClass.voidType)) {
            aroundCode.append("return ($r)point.getReturnValue();");
        }
        aroundCode.append("}");
        method.setBody(aroundCode.toString());
    }
}
