package com.flink.domain.dto;

import lombok.Data;

import java.util.List;


@Data
public class SyncDTO {
    /**
     * 源数据库地址
     */
    private String sourceDatabaseUrl;

    /**
     * 源数据库端口号
     */
    private String sourceDatabasePort;

    /**
     * 源数据库名
     */
    private String sourceDatabaseName;

    /**
     * 源数据库账号
     */
    private String sourceAccountNumber;

    /**
     * 源数据库密码
     */
    private String sourcePassWord;

    /**
     * 目标数据库地址
     */
    private String goalDatabaseUrl;

    /**
     * 目标数据库端口号
     */
    private String goalDatabasePort;

    /**
     * 目标数据库名
     */
    private String goalDatabaseName;

    /**
     * 目标数据库账号
     */
    private String goalAccountNumber;

    /**
     * 目标数据库密码
     */
    private String goalPassWord;

    /**
     * 数据同步的表名
     */
    private String tableName;

    /**
     * 数据同步的多个表名
     */
    private List<String> tableNames;
}
