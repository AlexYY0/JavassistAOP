package club.emperorws;

import club.emperorws.aop.DoAspect;
import club.emperorws.aop.toolkit.ClassUtils;
import club.emperorws.demo.BusinessController;
import club.emperorws.demo.aspect.annotation.CatchException;
import club.emperorws.demo.controller.BusinessController2;
import javassist.*;

import java.util.Set;

/**
 * @author: ${USER}
 * @date: ${DATE} ${TIME}
 * @description: ${NAME}: ${description}
 */
public class Main {
    public static void main(String[] args) throws Exception {
        //初始化
        DoAspect.init("club.emperorws.demo.aspect");
        //AOP字节码增强
        DoAspect.compileClass("club.emperorws.demo");
        //开始使用
        BusinessController bz = new BusinessController();
        bz.doSth("aa", 1);
        /*BusinessController2 bz2 = new BusinessController2();
        bz2.doSth("aa", 2);*/
        //demoTest();
    }

    private static void demoTest() throws Exception {
        // 获取默认的类池
        ClassPool pool = ClassPool.getDefault();
        Set<String> classNameList = ClassUtils.getClzFromPkg("club.emperorws.demo");
        CtClass clazz = pool.getOrNull(classNameList.toArray(new String[classNameList.size()])[classNameList.size() - 1]);
        System.out.println("clazz.getPackageName():" + clazz.getPackageName());
        System.out.println("clazz.getSimpleName():" + clazz.getSimpleName());
        System.out.println("clazz.getName():" + clazz.getName());
        CtMethod[] methods = clazz.getMethods();
        for (CtMethod method : methods) {
            if (method.hasAnnotation(CatchException.class)) {
                System.out.println("method.getLongName():" + method.getLongName());
                System.out.println("method.getName():" + method.getName());
                System.out.println("method.getSignature():" + method.getSignature());
                System.out.println("method.getGenericSignature():" + method.getGenericSignature());
                System.out.println("method.getReturnType():" + method.getReturnType().getName());
                System.out.println("method.getModifiers():" + Modifier.toString(method.getModifiers()));
                System.out.println("method info:" + Modifier.toString(method.getModifiers()) + " " + method.getReturnType().getName() + " " + method.getLongName());
                //1. 复制方法
                CtMethod srcMethod = CtNewMethod.copy(method, clazz, null);
                //2. 给新的方法换一个名字
                String srcMethodName = method.getName() + "$Proxy";
                srcMethod.setName(srcMethodName);
                //3. 将这个方法添加到类里面
                clazz.addMethod(srcMethod);
                StringBuilder sb = new StringBuilder();
                sb.append("{try{System.out.println(\"args:\"+$args);")
                        .append("return ").append(srcMethodName).append("($$);")
                        .append("}catch(Exception e){System.out.println(\"异常信息：\"+e.getMessage());return new club.emperorws.entity.R(\"1\",\"error\");}}");
                //4. 改变原有方法
                method.setBody(sb.toString());
                //method.addCatch("{ $e.getMessage(); throw $e;}", method.getExceptionTypes()[0]);
            }
        }
        //clazz.toClass();
        clazz.detach();
        //BusinessController bz = new BusinessController();
        //bz.doSth("aa", 2);
    }
}
