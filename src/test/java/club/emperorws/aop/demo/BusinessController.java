package club.emperorws.aop.demo;

import club.emperorws.aop.demo.aspect.annotation.CatchException;
import club.emperorws.aop.demo.aspect.annotation.CatchException2;
import club.emperorws.aop.entity.R;

/**
 * 业务方法接口
 *
 * @author: EmperorWS
 * @date: 2023/2/17 0:29
 * @description: BusinessController: 业务方法接口
 */
public class BusinessController {

    @CatchException
    @CatchException2
    public R doSth(String aa,Integer bb) throws Exception {
        System.out.println("Start doSth1-1");
        int a = 0, b = 1, c = 2;
        int r = a + b * c;
        //int e = 1 / 0;
        System.out.println("End doSth1-1");
        return new R("0", r);
    }

    public R doSth2(Integer aa,Integer bb) throws Exception {
        System.out.println("Start doSth1-2");
        int r = aa + bb;
        //int e = 1 / 0;
        System.out.println("End doSth1-2");
        return new R("0", r);
    }
}
