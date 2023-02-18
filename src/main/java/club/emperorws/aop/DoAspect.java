package club.emperorws.aop;

import club.emperorws.aop.annotation.*;
import club.emperorws.aop.constant.Constants;
import club.emperorws.aop.entity.AspectInfo;
import club.emperorws.aop.toolkit.ClassUtils;
import cn.hutool.core.lang.Console;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字节码动态编译切面，真正执行切面的方法
 *
 * @author: EmperorWS
 * @date: 2023/2/17 21:48
 * @description: DoAspect: 字节码动态编译切面，真正执行切面的方法
 */
public class DoAspect {

    /**
     * 所有的注解切面一一对应的集合
     * k：注解，v：切面信息
     */
    private static final Map<String, AspectInfo> ANNOTATION_ASPECT_MAP = new HashMap<>();

    /**
     * 初始化方法
     *
     * @param aspectPackageNames 需要扫描的切面编程包名集合：扫描{@link Aspect}注解的class类
     */
    public static void init(String... aspectPackageNames) {
        try {
            Set<String> classNameSet = new HashSet<>();
            //1. 包扫描，获取所有的class
            for (String aspectPackageName : aspectPackageNames) {
                classNameSet.addAll(ClassUtils.getClzFromPkg(aspectPackageName));
            }
            //2. 遍历所有的class，找出切面
            for (String className : classNameSet) {
                CtClass clazz = Constants.POOL.getOrNull(className);
                if (Objects.isNull(clazz)) {
                    continue;
                }
                //缓存匹配的注解-切面
                Aspect aspect = (Aspect) clazz.getAnnotation(Aspect.class);
                if (Objects.nonNull(aspect)) {
                    String pointcutClassPath = aspect.pointcutAnnotationClassPath();
                    ANNOTATION_ASPECT_MAP.put(pointcutClassPath, new AspectInfo(aspect.order(), clazz));
                }
            }
        } catch (ClassNotFoundException e) {
            Console.error(e, "DoAspect 初始化异常！");
        }
    }

    /**
     * 字节码动态编译切面，生成切面编程后的class代码
     *
     * @param pointcutPackageNames 需要扫描的切面点包名集合：扫描切点类，即标注自定义注解的类（使用AOP的class）
     */
    public static void compileClass(String... pointcutPackageNames) {
        try {
            Set<String> classNameSet = new HashSet<>();
            //1. 包扫描，获取所有的class
            for (String aspectPackageName : pointcutPackageNames) {
                classNameSet.addAll(ClassUtils.getClzFromPkg(aspectPackageName));
            }
            //2. 遍历所有的class，找出切点
            for (String className : classNameSet) {
                //判断是否重新加载字节码对象（为了解决重复加载class的问题）
                boolean isLoadClass = false;
                CtClass clazz = Constants.POOL.getOrNull(className);
                if (Objects.isNull(clazz)) {
                    continue;
                }
                //遍历class的所有方法
                CtMethod[] methods = clazz.getDeclaredMethods();
                for (CtMethod method : methods) {
                    isLoadClass = methodAspect(clazz, method);
                }
                //加载编好的class
                if (isLoadClass) {
                    clazz.toClass();
                    //clazz.writeFile("E:\\JavaMainTest\\target\\classes\\");
                    clazz.detach();
                }
            }
        } catch (Exception e) {
            Console.error(e, "DoAspect 字节码编程class异常！");
        }
    }

    /**
     * 为方法添加切面编程
     *
     * @param clazz  切点类
     * @param method 切点方法
     * @return 是否重新加载class字节码对象
     * @throws Exception 异常
     */
    private static boolean methodAspect(CtClass clazz, CtMethod method) throws Exception {
        boolean isLoadClass;
        //1. 遍历class方法里的所有注解
        List<AspectInfo> methodAspectList = new ArrayList<>();
        Object[] methodAnnotationObjs = method.getAvailableAnnotations();
        for (Object methodAnnotationObj : methodAnnotationObjs) {
            Annotation methodAnnotation = ClassUtils.getProxyAnnotation(methodAnnotationObj);
            //存在切面编程，找出匹配的切面
            if (ANNOTATION_ASPECT_MAP.containsKey(methodAnnotation.getTypeName())) {
                methodAspectList.add(ANNOTATION_ASPECT_MAP.get(methodAnnotation.getTypeName()));
            }
        }
        //2. 按照优先级排序
        List<AspectInfo> sortedMethodAspectList = methodAspectList.stream()
                .sorted(Comparator.comparing(AspectInfo::getOrder).reversed())
                .collect(Collectors.toList());
        isLoadClass = !sortedMethodAspectList.isEmpty();
        //3. 开始切面编程
        codeByAnnotation(clazz, method, sortedMethodAspectList);
        return isLoadClass;
    }

    /**
     * 根据切面注解，动态生成字节码
     *
     * @param clazz                  切点类
     * @param method                 切点方法
     * @param sortedMethodAspectList 切点方法的所有注解
     * @throws CannotCompileException 异常
     * @throws NotFoundException      异常
     */
    private static void codeByAnnotation(CtClass clazz, CtMethod method, List<AspectInfo> sortedMethodAspectList) throws CannotCompileException, NotFoundException {
        //切面注解不为空，判断方法是不是static方法
        boolean methodIsStatic = Modifier.isStatic(method.getModifiers());
        //1. 开始编辑字节码，实现切面编程
        for (AspectInfo methodAspectInfo : sortedMethodAspectList) {
            //2. 获取切面
            CtClass aspectClass = methodAspectInfo.getAspectClass();
            //3. 获取切面的所有方法，并遍历切面的所有方法，找出对应的切面编程
            CtMethod[] aspectMethods = aspectClass.getDeclaredMethods();
            for (CtMethod aspectMethod : aspectMethods) {
                //前置通知
                aspectBefore(method, aspectClass, aspectMethod);
                //返回通知
                aspectAfterReturning(method, aspectClass, aspectMethod);
                //后置通知
                aspectAfter(method, aspectClass, aspectMethod);
                //异常通知
                aspectAfterThrowing(method, aspectClass, aspectMethod);
                //环绕通知
                aspectAround(clazz, method, methodIsStatic, aspectClass, aspectMethod);
            }

        }
    }

    /**
     * 前置通知字节码增强
     *
     * @param method       方法
     * @param aspectClass  切面class
     * @param aspectMethod {@link Before}标注的方法
     * @throws CannotCompileException 异常
     */
    private static void aspectBefore(CtMethod method, CtClass aspectClass, CtMethod aspectMethod) throws CannotCompileException {
        if (aspectMethod.hasAnnotation(Before.class)) {
            String beforeCode = "" +
                    "Class clazz = $class;" +
                    "java.lang.reflect.Method method = clazz.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                    "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(null,method,$args);" +
                    "Object aspectClass = Class.forName(\"" + aspectClass.getName() + "\").newInstance();" +
                    "java.lang.reflect.Method beforeMethod = aspectClass.getClass().getDeclaredMethod(\"" + aspectMethod.getName() + "\", new Class[]{club.emperorws.aop.entity.Pointcut.class});" +
                    "beforeMethod.invoke(aspectClass, new Object[]{point});";
            method.insertBefore(beforeCode);
        }
    }

    /**
     * 返回通知字节码增强
     *
     * @param method       方法
     * @param aspectClass  切面class
     * @param aspectMethod {@link AfterReturning}AfterReturning标注的方法
     * @throws CannotCompileException 异常
     */
    private static void aspectAfterReturning(CtMethod method, CtClass aspectClass, CtMethod aspectMethod) throws CannotCompileException {
        if (aspectMethod.hasAnnotation(AfterReturning.class)) {
            String afterReturningCode = "" +
                    "Class clazz = $class;" +
                    "java.lang.reflect.Method method = clazz.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                    "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(null,method,$args,$_);" +
                    "Object aspectClass = Class.forName(\"" + aspectClass.getName() + "\").newInstance();" +
                    "java.lang.reflect.Method afterReturningMethod = aspectClass.getClass().getDeclaredMethod(\"" + aspectMethod.getName() + "\", new Class[]{club.emperorws.aop.entity.Pointcut.class});" +
                    "afterReturningMethod.invoke(aspectClass, new Object[]{point});";
            method.insertAfter(afterReturningCode);
        }
    }

    /**
     * 后置通知字节码增强
     *
     * @param method       方法
     * @param aspectClass  切面class
     * @param aspectMethod {@link After}After标注的方法
     * @throws CannotCompileException 异常
     */
    private static void aspectAfter(CtMethod method, CtClass aspectClass, CtMethod aspectMethod) throws CannotCompileException {
        if (aspectMethod.hasAnnotation(After.class)) {
            String afterCode = "" +
                    "Class clazz = $class;" +
                    "java.lang.reflect.Method method = clazz.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                    "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(null,method,$args);" +
                    "Object aspectClass = Class.forName(\"" + aspectClass.getName() + "\").newInstance();" +
                    "java.lang.reflect.Method afterMethod = aspectClass.getClass().getDeclaredMethod(\"" + aspectMethod.getName() + "\", new Class[]{club.emperorws.aop.entity.Pointcut.class});" +
                    "afterMethod.invoke(aspectClass, new Object[]{point});";
            method.insertAfter(afterCode);
        }
    }

    /**
     * 异常通知字节码增强
     *
     * @param method       方法
     * @param aspectClass  切面class
     * @param aspectMethod {@link AfterThrowing}AfterThrowing标注的方法
     * @throws CannotCompileException 异常
     * @throws NotFoundException      异常
     */
    private static void aspectAfterThrowing(CtMethod method, CtClass aspectClass, CtMethod aspectMethod) throws CannotCompileException, NotFoundException {
        if (aspectMethod.hasAnnotation(AfterThrowing.class)) {
            String exceptionCode = "" +
                    "Class clazz = $class;" +
                    "java.lang.reflect.Method method = clazz.getDeclaredMethod(\"" + method.getName() + "\",$sig);" +
                    "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(null,method,$args,$e);" +
                    "Object aspectClass = Class.forName(\"" + aspectClass.getName() + "\").newInstance();" +
                    "java.lang.reflect.Method exceptionMethod = aspectClass.getClass().getDeclaredMethod(\"" + aspectMethod.getName() + "\", new Class[]{club.emperorws.aop.entity.Pointcut.class});" +
                    "exceptionMethod.invoke(aspectClass, new Object[]{point});" +
                    "throw $e;";
            method.addCatch(exceptionCode, Constants.POOL.get("java.lang.Exception"));
        }
    }

    /**
     * 环绕通知字节码增强
     *
     * @param clazz          切点类
     * @param method         切点方法
     * @param methodIsStatic 方法是否是静态方法
     * @param aspectClass    切面class
     * @param aspectMethod   {@link Around}Around标志的方法
     * @throws NotFoundException      异常
     * @throws CannotCompileException 异常
     */
    private static void aspectAround(CtClass clazz, CtMethod method, boolean methodIsStatic, CtClass aspectClass, CtMethod aspectMethod) throws NotFoundException, CannotCompileException {
        if (aspectMethod.hasAnnotation(Around.class)) {
            //先复制原方法，由于嵌套执行
            //1. 复制方法
            CtMethod srcMethod = CtNewMethod.copy(method, clazz, null);
            //2. 给新的方法换一个名字
            String srcMethodName = method.getName() + "_" + UUID.randomUUID().toString().replace("-", "");
            srcMethod.setName(srcMethodName);
            //3. 复制运行时可见注解
            srcMethod.getMethodInfo().addAttribute(method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag));
            //4. 将这个方法添加到类里面
            clazz.addMethod(srcMethod);
            //5. 再嵌套设置新方法
            StringBuilder aroundCode = new StringBuilder("{" +
                    "Class clazz = $class;" +
                    "java.lang.reflect.Method method = clazz.getDeclaredMethod(\"" + srcMethodName + "\",$sig);" +
                    "club.emperorws.aop.entity.Pointcut point = new club.emperorws.aop.entity.Pointcut(" + (methodIsStatic ? "" : "$0,") + "method,$args);" +
                    "Object aspectClass = Class.forName(\"" + aspectClass.getName() + "\").newInstance();" +
                    "java.lang.reflect.Method beforeMethod = aspectClass.getClass().getDeclaredMethod(\"" + aspectMethod.getName() + "\", new Class[]{club.emperorws.aop.entity.Pointcut.class});" +
                    "beforeMethod.invoke(aspectClass, new Object[]{point});");
            //非void的方法，增加return返回值
            if (!method.getReturnType().equals(CtClass.voidType)) {
                aroundCode.append("return ($r)point.getReturnValue();");
            }
            aroundCode.append("}");
            method.setBody(aroundCode.toString());
        }
    }
}
