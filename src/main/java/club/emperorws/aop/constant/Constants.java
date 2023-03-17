package club.emperorws.aop.constant;

import javassist.ClassPool;

/**
 * 一些常量
 *
 * @author: EmperorWS
 * @date: 2023/2/18 2:19
 * @description: Constants: 一些常量
 */
public interface Constants {

    /**
     * 字节码class池
     */
    ClassPool POOL = ClassPool.getDefault();

    /**
     * 切点方法签名
     */
    String METHOD_SIGNATURE = "methodSignature";

    /**
     * 切点方法注解名称
     */
    String METHOD_ANNOTATION_NAME = "methodAnnotationName";
}
