package club.emperorws.aop.toolkit;

import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 与Class文件有关的工具类
 *
 * @author: EmperorWS
 * @date: 2023/2/17 22:22
 * @description: ClassUtils: 与Class文件有关的工具类
 */
public class ClassUtils {

    /**
     * 从包package中获取所有的Class
     *
     * @param pkg
     * @return
     */
    public static Set<String> getClzFromPkg(String pkg) {
        //第一个class类的集合
        Set<String> classNameSet = new HashSet<>();
        // 获取包的名字 并进行替换
        String pkgDirName = pkg.replace('.', '/');
        try {
            Enumeration<URL> urls = ClassUtils.class.getClassLoader().getResources(pkgDirName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findClassesByFile(pkg, filePath, classNameSet);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 获取jar
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    //扫描jar包文件 并添加到集合中
                    findClassesByJar(pkg, jar, classNameSet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classNameSet;
    }

    /**
     * 获取文件夹下的class文件
     *
     * @param pkgName      包路径
     * @param pkgPath      包路径的绝对地址
     * @param classNameSet class文件集合
     */
    private static void findClassesByFile(String pkgName, String pkgPath, Set<String> classNameSet) {
        // 获取此包的目录 建立一个File
        File dir = new File(pkgPath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith("class"));

        if (Objects.isNull(dirfiles) || dirfiles.length == 0) {
            return;
        }

        String className;
        // 循环所有文件
        for (File f : dirfiles) {
            // 如果是目录 则继续扫描
            if (f.isDirectory()) {
                findClassesByFile(pkgName + "." + f.getName(), pkgPath + "/" + f.getName(), classNameSet);
                continue;
            }
            // 如果是java类文件 去掉后面的.class 只留下类名
            className = f.getName();
            className = pkgName + "." + className.substring(0, className.length() - 6);
            //添加类
            classNameSet.add(className);
        }
    }

    private static void findClassesByJar(String pkgName, JarFile jar, Set<String> classNameSet) {
        String pkgDir = pkgName.replace(".", "/");
        // 从此jar包 得到一个枚举类
        Enumeration<JarEntry> entry = jar.entries();

        JarEntry jarEntry;
        String name, className;
        Class<?> claze;
        // 同样的进行循环迭代
        while (entry.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文
            jarEntry = entry.nextElement();

            name = jarEntry.getName();
            // 如果是以/开头的
            if (name.charAt(0) == '/') {
                // 获取后面的字符串
                name = name.substring(1);
            }

            if (jarEntry.isDirectory() || !name.startsWith(pkgDir) || !name.endsWith(".class")) {
                continue;
            }
            //如果是一个.class文件 而且不是目录
            // 去掉后面的".class" 获取真正的类名
            className = pkgName + "." + name.substring(0, name.length() - 6);
            //添加类
            classNameSet.add(className);
        }
    }

    /**
     * 加载类
     *
     * @param fullClzName 类全名
     * @return 加载类
     */
    private static Class<?> loadClass(String fullClzName) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(fullClzName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据注解的代理对象，获取原注解信息
     *
     * @param proxyAnnotation 代理注解
     * @return 被代理的注解信息
     * @throws Exception 异常
     */
    public static Annotation getProxyAnnotation(Object proxyAnnotation) throws Exception {
        Field field = proxyAnnotation.getClass().getSuperclass().getDeclaredField("h");
        field.setAccessible(true);
        //获取指定对象中此字段的值
        AnnotationImpl annotationImpl = (AnnotationImpl) field.get(proxyAnnotation);
        return annotationImpl.getAnnotation();
    }
}
