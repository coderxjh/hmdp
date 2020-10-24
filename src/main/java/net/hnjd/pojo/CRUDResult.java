package net.hnjd.pojo;

/**
 * @author simba@onlying.cn
 * @date 2020/10/22 20:58
 */
public class CRUDResult {

    private int success = 1;
    private String msg = "";

    public CRUDResult() {
    }

    public CRUDResult(int success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
