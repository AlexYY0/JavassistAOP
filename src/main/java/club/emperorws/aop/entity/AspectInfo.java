package club.emperorws.aop.entity;

import javassist.CtClass;

/**
 * 存储切面信息的实体
 *
 * @author: EmperorWS
 * @date: 2023/2/17 23:17
 * @description: AspectInfo: 存储切面信息的实体
 */
public class AspectInfo {

    /**
     * 切面的执行优先级
     */
    private int order;

    /**
     * 切面的class
     */
    private CtClass aspectClass;

    public AspectInfo() {
    }

    public AspectInfo(int order, CtClass aspectClass) {
        this.order = order;
        this.aspectClass = aspectClass;
    }

    public CtClass getAspectClass() {
        return aspectClass;
    }

    public void setAspectClass(CtClass aspectClass) {
        this.aspectClass = aspectClass;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "AspectInfo{" +
                "aspectClass=" + aspectClass.getName() +
                ", order=" + order +
                '}';
    }
}
