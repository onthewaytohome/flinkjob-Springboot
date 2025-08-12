package com.flink.common.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum ReturnStatus {
    SUCCESS("操作成功", 200),
    OPERAFAIL("操作失败", 201),
    DISTINCT("存在重复数据", 202),
    FILENOTFIND("文件找不到", 203),
    NoUser("用户不存在", 204),
    ContentError("内容格式错误", 205),
    RECORD_NOT_FOUND("记录不存在", 206),
    ValidateFailure("参数验证错误", 400),
    SessionTimeout("会话超时", 401),
    LoginFailure("登录失败", 402),
    NoRight("权限不足",403),
    InvalidCSRF("无效的保护令牌",404),
    NoIntentResult("意图识别没有结果",405),
    MoreFieldIntentResult("意图命中多个字段",408),
    RequestMissingBodyError("缺少请求体!",1004),
    ParameterMissingError("确少必要请求参数!",1003),
    ParameterError("请求参数有误!",1002),
    NotFountResource("没有找到相关资源!",1001),
    Error("服务器错误", 500),
    REQUEST_PARAM_ERROR("请求错误",501),
    NULL_POINTER_EXCEPTION("空指针异常",406),
    PARSE_EXCEPTION("格式解析异常",507),
    ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION("超出索引异常",409),
    SQL_EXCEPTION("SQL异常",410),
    NETWORK_BUSY("内部服务器异常",411),
    SQL_EXCEPTION_MESSAGE("数据库执行异常",808),
    DATE_FORMAT_ERROR("时间格式异常",897),
    DATABASE_CONNECT_OUT_TIME("数据库连接超时",900),
    BIG_MODEL_ERROR("大模型调用失败",1001),
    PYTHON_ERROR("智能辅助暂时无法使用，请自行输入!",1002),
    REFUSE_ERROR("请求被拒绝，服务器未开启",1003),
    JSON_TYPE_ERROR("JSON相关异常",1004),
    UNRECO_GNIZED("未识别出主症状", 1005),
    MODEL_FAIL("智能辅助暂时无法使用，请自行输入!", 1006),
    UNKNOWM_ERROR("位置异常",1400),
    PYTHON_BERT_ERROR("python的bert_ner_Error",1010),
    HIS_INFORM_ERROR("his服务的预警任务异常",1011),
    DINGTALK_REPORT_ERROR("钉钉机器人秘钥错误",1012);


    private final String name;

    private final Integer value;

    private ReturnStatus(String name, Integer value) {
        this.name = name;
        this.value = value;
    }
    @Override
    public String toString() {
        return value.toString();
    }

    public static ReturnStatus valueOf(int value) {
        //手写的从int到enum的转换函数
        switch (value) {
            case 200:
                return SUCCESS;
            case 201:
                return OPERAFAIL;
            case 202:
                return DISTINCT;
            case 203:
                return FILENOTFIND;
            case 204:
                return NoUser;
            case 205:
                return ContentError;
            case 206:
                return RECORD_NOT_FOUND;
            case 401:
                return SessionTimeout;
            case 402:
                return LoginFailure;
            case 403:
                return NoRight;
            case 500:
                return Error;
            case 406:
                return NULL_POINTER_EXCEPTION;
            case 507:
                return PARSE_EXCEPTION;
            case 409:
                return ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
            case 410:
                return SQL_EXCEPTION;
            case 897:
                return DATE_FORMAT_ERROR;
            case 900:
                return DATABASE_CONNECT_OUT_TIME;
            case 1003:
                return REFUSE_ERROR;
            case  808:
                return SQL_EXCEPTION_MESSAGE;
            case 1004:
                return JSON_TYPE_ERROR;
            case 1002:
                return PYTHON_ERROR;
            case 1001:
                return BIG_MODEL_ERROR;
            case 1010:
                return PYTHON_BERT_ERROR;
            default:
                return null;
        }
    }

    public static List<String> returnErrorType(){
        List<String> errorType = new ArrayList<>();
        for (ReturnStatus value : ReturnStatus.values()) {
            if (value.getValue()!=200){
                errorType.add(value.getValue().toString());
            }
        }
        return errorType;
    }

}
