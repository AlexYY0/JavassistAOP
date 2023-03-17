package club.emperorws.aop.toolkit;

import club.emperorws.aop.toolkit.expression.AnnotationFunction;
import club.emperorws.aop.toolkit.expression.ExecutionFunction;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;

import java.util.Map;

/**
 * 表达式工具
 *
 * @author: EmperorWS
 * @date: 2023/3/16 22:07
 * @description: ExprUtils: 表达式工具
 */
public class ExprUtils {

    private static final AviatorEvaluatorInstance ENGINE = AviatorEvaluator.getInstance();

    /**
     * 初始化表达式引擎
     */
    static {
        ENGINE.addFunction(new ExecutionFunction());
        ENGINE.addFunction(new AnnotationFunction());
    }

    /**
     * 通过表达式引擎执行表达式
     *
     * @param expr   表达式
     * @param params 表达式对应的参数变量
     * @param <T>    返回结果
     * @return 执行结果
     */
    public static <T> T executeExpression(String expr, Map<String, Object> params) {
        return (T) ENGINE.execute(expr, params);
    }
}
