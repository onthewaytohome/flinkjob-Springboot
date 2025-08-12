package com.flink.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class TableMysqlToMysqlDTO {
    /**
     * 源数据库地址
     */
    @ApiModelProperty("源数据库地址")
    private String sourceDatabaseUrl;
    /**
     * 源数据库端口号
     */
    @ApiModelProperty("源数据库端口号")
    private String sourceDatabasePort;
    /**
     * 源数据库名
     */
    @ApiModelProperty("源数据库名")
    private String sourceDatabaseName;
    /**
     * 源数据库账号
     */
    @ApiModelProperty("源数据库账号")
    private String sourceAccountNumber;
    /**
     * 源数据库密码
     */
    @ApiModelProperty("源数据库密码")
    private String sourcePassWord;
    /**
     * 目标数据库地址
     */
    @ApiModelProperty("目标数据库地址")
    private String goalDatabaseUrl;
    /**
     * 目标数据库端口号
     */
    @ApiModelProperty("目标数据库端口号")
    private String goalDatabasePort;
    /**
     * 目标数据库名
     */
    @ApiModelProperty("目标数据库名")
    private String goalDatabaseName;
    /**
     * 目标数据库账号
     */
    @ApiModelProperty("目标数据库账号")
    private String goalAccountNumber;
    /**
     * 目标数据库密码
     */
    @ApiModelProperty("目标数据库密码")
    private String goalPassWord;
    /**
     * 数据同步的表名
     */
    @ApiModelProperty("数据同步的表名")
    private String tableName;
    /**
     * 时间字段
     */
    @ApiModelProperty("时间字段")
    private String timeField;
    /**
     * 起始时间
     */
    @ApiModelProperty("起始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /**
     * 结尾时间
     */
    @ApiModelProperty("结尾时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    /**
     * 同步的字段名
     */
    @ApiModelProperty("同步的字段名")
    private List<String> syncFields;

}
