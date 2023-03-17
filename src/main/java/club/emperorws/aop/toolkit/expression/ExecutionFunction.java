package club.emperorws.aop.toolkit.expression;

import club.emperorws.aop.constant.Constants;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式引擎，自定义execution函数
 *
 * @author: EmperorWS
 * @date: 2023/3/17 19:59
 * @description: ExecutionFunction: 表达式引擎，自定义execution函数
 */
public class ExecutionFunction extends AbstractFunction {

    /**
     * 自定义方法的名称：execution("")
     *
     * @return 自定义方法得名称：execution("")
     */
    @Override
    public String getName() {
        return "execution";
    }

    /**
     * 自定义方法execution("")的执行逻辑
     *
     * @param env  请求参数，环境变量
     * @param arg1 实际的请求参数
     * @return 自定义方法execution("")执行的返回结果
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        //表达式，效果等同于FunctionUtils.getStringValue(arg1,env)
        //正则表达式转义
        String expr = arg1.stringValue(env).replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
        //获取校验参戴
        String methodSignature = env.getOrDefault(Constants.METHOD_SIGNATURE, "").toString();
        Pattern pattern = Pattern.compile(expr);
        Matcher matcher = pattern.matcher(methodSignature);
        return matcher.find() ? AviatorBoolean.TRUE : AviatorBoolean.FALSE;
    }
}
