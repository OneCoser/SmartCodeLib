package ch.smart.code.network;

public final class ResponseConfig {
    /**
     * 成功状态码
     * 各项目可根据自己的业务需求在外部赋值: ResponseConfig.SUCCESS_STATUS_CODE = "200"
     */
    public static String SUCCESS_STATUS_CODE = "200";

    /**
     * 状态码的字段名
     * 各项目可根据自己的业务需求在外部赋值: ResponseConfig.STATUS_CODE_FIELD = "code"
     */
    public static String CODE_FIELD = "code";

    /**
     * 错误信息的字段名
     * 各项目可根据自己的业务需求在外部赋值: ResponseConfig.MSG_FIELD = "msg"
     */
    public static String MSG_FIELD = "msg";

    /**
     * 数据泛型的字段名
     * 各项目可根据自己的业务需求在外部赋值: ResponseConfig.DATA_FIELD = "data"
     */
    public static String DATA_FIELD = "data";
}
