package com.flink.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
public class ToManyTablesDTO {
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
     * 表的同步信息
     */
    @ApiModelProperty("表的同步信息")
    private List<TableMessageDTO> dtoList;
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


}
