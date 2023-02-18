package club.emperorws;

import club.emperorws.aop.DoAspect;
import club.emperorws.demo.BusinessController;
import club.emperorws.demo.aspect.annotation.CatchException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

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
        bz.doSth("aa", 2);
    }

    private void demoTest() throws Exception {
        // 获取默认的类池
        ClassPool pool = ClassPool.getDefault();
        List<String> classNameList = getClassFile("club.emperorws.bz");
        CtClass clazz = pool.getOrNull(classNameList.get(0));
        System.out.println("clazz.getPackageName():" + clazz.getPackageName());
        System.out.println("clazz.getSimpleName():" + clazz.getSimpleName());
        System.out.println("clazz.getName():" + clazz.getName());
        CtMethod[] methods = clazz.getMethods();
        for (CtMethod method : methods) {
            if (method.hasAnnotation(CatchException.class)) {
                System.out.println("method.getLongName():" + method.getLongName());
                System.out.println("method.getName():" + method.getName());
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
        clazz.toClass();
        clazz.detach();
        BusinessController bz = new BusinessController();
        bz.doSth("aa", 2);
    }

    private static List<String> getClassFile(String pkg) throws UnsupportedEncodingException {
        List<String> classNameList = new ArrayList<>();

        String pkgDirName = pkg.replace('.', '/');
        URL resource = Main.class.getClassLoader().getResource(pkgDirName);
        // 获取包的物理路径
        String pkgPath = URLDecoder.decode(resource.getFile(), "UTF-8");
        File dir = new File(pkgPath);
        File[] dirfiles = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith("class"));
        for (File classFile : dirfiles) {
            String classFileName = classFile.getName();
            String className = classFileName.substring(0, classFileName.length() - 6);
            classNameList.add(pkg + "." + className);
        }
        return classNameList;
    }
}
