package club.emperorws.aop.toolkit.expression;

import club.emperorws.aop.constant.Constants;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 表达式引擎，自定义annotation函数
 *
 * @author: EmperorWS
 * @date: 2023/3/17 19:44
 * @description: AnnotationFunction: 表达式引擎，自定义annotation函数
 */
public class AnnotationFunction extends AbstractFunction {

    /**
     * 自定义方法的名称：annotation("")
     *
     * @return 自定义方法得名称：annotation("")
     */
    @Override
    public String getName() {
        return "annotation";
    }

    /**
     * 自定义方法annotation("")的执行逻辑
     *
     * @param env  请求参数，环境变量
     * @param arg1 实际的请求参数
     * @return 自定义方法annotation("")执行的返回结果
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        //表达式，效果等同于FunctionUtils.getStringValue(arg1,env)
        String expr = arg1.stringValue(env);
        //获取校验参数
        String methodAnnotationName = env.getOrDefault(Constants.METHOD_ANNOTATION_NAME, "").toString();
        return methodAnnotationName.equals(expr) ? AviatorBoolean.TRUE : AviatorBoolean.FALSE;
    }
}
