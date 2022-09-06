package com.tagging.enums;
import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    UNREGISTERED(false,105,"未注册"),
    SUCCESS(true,2000,"成功"),
    UNKNOWN_ERROR(false,2001,"未知错误"),
    PARAM_ERROR(false,2002,"参数错误"),
    NULL_POINT(false,2003,"空指针异常"),
    HTTP_CLIENT_ERROR(false,2004,"客户端连接异常"),
    USER_ERROR(false,103,"未找到该用户!"),

    NO_TOKEN(false,401,"无token"),
    CANNOT_GET_SECREAT(false,402,"无法从数据库获取私钥"),
    TOKEN_DECRYPTION_ERROR(false,403,"token解密失误"),
    UNIQUENESS_TEMPLATE(false,4002,"模板类型唯一且已存在使用种模板"),
    TOKEN_EXECUTE_ERROR(false,102,"token异常!请重新登陆"),
    CANNOT_DELETE_TEMPLATE(false,4001,"模板使用中，删除失败");

    /**
    /**
     * 响应是否成功
     */
    private Boolean success;
    /**
     * 响应状态码
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String message;

    ResultCodeEnum(boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
