package club.emperorws.aop.entity;

/**
 * 返回结果封装
 *
 * @author: EmperorWS
 * @date: 2023/2/17 0:36
 * @description: R: 返回结果封装
 */
public class R {
    String code;
    Object data;

    public R() {
    }

    public R(String code, Object data) {
        this.code = code;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "R{" +
                "code='" + code + '\'' +
                ", data=" + data +
                '}';
    }
}
