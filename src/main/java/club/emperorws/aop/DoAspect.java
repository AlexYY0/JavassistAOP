package club.emperorws.aop;

import club.emperorws.aop.annotation.Aspect;
import club.emperorws.aop.constant.Constants;
import club.emperorws.aop.entity.AspectInfo;
import club.emperorws.aop.toolkit.AspectUtils;
import club.emperorws.aop.toolkit.ClassUtils;
import club.emperorws.aop.toolkit.ExprUtils;
import cn.hutool.core.lang.Console;
import javassist.*;
import javassist.bytecode.annotation.Annotation;

import java.util.*;
import java.util.stream.Collectors;

import static club.emperorws.aop.toolkit.AspectUtils.*;

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

    private DoAspect() {
        throw new IllegalStateException("Utility DoAspect class can not use constructor.");
    }

    /**
     * 初始化方法
     *
     * @param aspectPackageNames 需要扫描的切面编程包名集合：扫描{@link Aspect}注解的class类
     */
    public static void init(String... aspectPackageNames) {
        //释放内存，减小的内存损耗
        CtClass clazz = null;
        try {
            Set<String> classNameSet = new HashSet<>();
            //1. 包扫描，获取所有的class
            for (String aspectPackageName : aspectPackageNames) {
                classNameSet.addAll(ClassUtils.getClzFromPkg(aspectPackageName));
            }
            //2. 遍历所有的class，找出切面
            for (String className : classNameSet) {
                clazz = Constants.POOL.getOrNull(className);
                if (Objects.isNull(clazz)) {
                    continue;
                }
                //缓存匹配的注解-切面
                Aspect aspect = (Aspect) clazz.getAnnotation(Aspect.class);
                if (Objects.nonNull(aspect)) {
                    ANNOTATION_ASPECT_MAP.put(aspect.pointcut().replaceAll("@", ""), new AspectInfo(aspect.order(), clazz));
                }
                clazz.detach();
            }
        } catch (Exception e) {
            if (Objects.nonNull(clazz)) {
                clazz.detach();
            }
            Console.error(e, "DoAspect 初始化异常！");
        }
    }

    /**
     * 字节码动态编译切面，生成切面编程后的class代码
     *
     * @param pointcutPackageNames 需要扫描的切面点包名集合：扫描切点类，即标注自定义注解的类（使用AOP的class）
     */
    public static void compileClass(String... pointcutPackageNames) {
        //释放内存，减小的内存损耗
        CtClass clazz = null;
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
                clazz = Constants.POOL.getOrNull(className);
                if (Objects.isNull(clazz)) {
                    continue;
                }
                //遍历class的所有方法
                CtMethod[] methods = clazz.getDeclaredMethods();
                for (CtMethod method : methods) {
                    //私有方法不允许AOP切面编程
                    if (Modifier.isPrivate(method.getModifiers())) {
                        continue;
                    }
                    boolean flag = methodAspect(clazz, method);
                    //避免重复definition Class而报错
                    if (!isLoadClass) {
                        isLoadClass = flag;
                    }
                }
                //加载编好的class
                if (isLoadClass) {
                    clazz.toClass();
                    clazz.writeFile("E:\\CodePractice\\StudyPractice\\JavaMainTest\\target\\classes\\javassist\\");
                }
                clazz.detach();
            }
        } catch (Exception e) {
            if (Objects.nonNull(clazz)) {
                clazz.detach();
            }
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
        //1. 遍历class方法里的所有注解
        List<AspectInfo> methodAspectList = new ArrayList<>();
        Object[] methodAnnotationObjs = method.getAvailableAnnotations();
        //表达式参数
        Map<String, Object> context = new HashMap<>(2);
        context.put(Constants.METHOD_SIGNATURE, AspectUtils.getMethodInfoStr(method));
        if (Objects.nonNull(methodAnnotationObjs) && methodAnnotationObjs.length != 0) {
            //有注解时的表达式执行：@annotation + 其他
            for (Object methodAnnotationObj : methodAnnotationObjs) {
                Annotation methodAnnotation = ClassUtils.getProxyAnnotation(methodAnnotationObj);
                context.put(Constants.METHOD_ANNOTATION_NAME, methodAnnotation.getTypeName());
                findAspectClass(methodAspectList, context);
            }
        } else {
            //其他非注解时的表达式执行：execution
            findAspectClass(methodAspectList, context);
        }
        //2. 按照优先级排序
        List<AspectInfo> sortedMethodAspectList = methodAspectList.stream()
                .sorted(Comparator.comparing(AspectInfo::getOrder).reversed())
                .collect(Collectors.toList());
        if (sortedMethodAspectList.isEmpty()) {
            return false;
        }
        //3. 开始切面编程
        codeByAnnotation(clazz, method, sortedMethodAspectList);
        return true;
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
    private static void codeByAnnotation(CtClass clazz, CtMethod method, List<AspectInfo> sortedMethodAspectList) throws CannotCompileException, NotFoundException, ClassNotFoundException {
        //切面注解不为空，判断方法是不是static方法
        boolean methodIsStatic = Modifier.isStatic(method.getModifiers());
        //1. 开始编辑字节码，实现切面编程
        for (AspectInfo methodAspectInfo : sortedMethodAspectList) {
            //2. 获取切面
            CtClass aspectClass = methodAspectInfo.getAspectClass();
            //3. 获取切面编程方法
            Map<Class<?>, CtMethod> methodMap = makeAspectOrder(aspectClass);
            //创建前置局部变量
            createLocalVariables(aspectClass, method);
            //前置通知
            aspectBefore(method, aspectClass, methodMap);
            //返回通知
            aspectAfterReturning(method, aspectClass, methodMap);
            //异常通知
            aspectAfterThrowing(method, aspectClass, methodMap);
            //后置通知
            aspectAfter(method, aspectClass, methodMap);
            //环绕通知
            aspectAround(clazz, method, methodIsStatic, aspectClass, methodMap);
        }
    }

    /**
     * 找出切点method
     *
     * @param methodAspectList 切点method集合
     * @param context          表达式环境变量
     */
    private static void findAspectClass(List<AspectInfo> methodAspectList, Map<String, Object> context) {
        //存在切面编程，找出匹配的切面
        for (Map.Entry<String, AspectInfo> entry : ANNOTATION_ASPECT_MAP.entrySet()) {
            String pointcut = entry.getKey();
            //规则匹配，是切点
            if (Boolean.TRUE.equals(ExprUtils.<Boolean>executeExpression(pointcut, context))) {
                methodAspectList.add(entry.getValue());
            }
        }
    }
}
