package com.flink.common.util;

import com.flink.domain.dto.SyncDTO;
import com.flink.domain.dto.TableMysqlToMysqlDTO;
import com.flink.domain.dto.ToManyTablesDTO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MysqlConvertUtil {

    public static void  flinkMysqlToMysql(String sourceDDL,String goalDDL,String sourceTableName,String goalTableName){
        StreamExecutionEnvironment env = null;
        // 本机执行环境
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 远程执行环境
        //env = StreamExecutionEnvironment.createRemoteEnvironment("172.18.0.75", 7878);
        env.enableCheckpointing(30000L).setParallelism(2);
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        tableEnv.executeSql(sourceDDL);

        tableEnv.executeSql(goalDDL);

        // 将source_test同步到sink_test和sink_test_second
        tableEnv.getConfig().set("pipeline.name", String.format("Flink %s To %s",sourceTableName,goalTableName ));         // 设置job名称

        //同步数据
        tableEnv.executeSql(String.format("insert into %s select * from %s;",goalTableName,sourceTableName));

//        //打印数据变更
        tableEnv.executeSql("select * from source_test;").print();
    }


    public static void  flinkMysqlToMysqlTime(String sourceDDL,String goalDDL,String sourceTableName,String goalTableName,String timeField,String startTime,String endTime){
        StreamExecutionEnvironment env = null;
        Configuration config = new Configuration();
        config.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 3); // 尝试重启次数
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofSeconds(10)); // 延时
        // 本机执行环境
        env = StreamExecutionEnvironment.getExecutionEnvironment(config);
        // 远程执行环境
        //env = StreamExecutionEnvironment.createRemoteEnvironment("172.18.0.75", 7878);
        env.enableCheckpointing(60000L).setParallelism(2);
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        tableEnv.getConfig().setLocalTimeZone(ZoneId.of("Asia/Shanghai"));

        tableEnv.executeSql(sourceDDL);

        tableEnv.executeSql(goalDDL);

        // 将source_test同步到sink_test和sink_test_second
        tableEnv.getConfig().set("pipeline.name", String.format("Flink %s To %s",sourceTableName,goalTableName ));         // 设置job名称
        String executeSql="";
        if (StringUtils.isNotEmpty(timeField) && StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)){
            executeSql=String.format("insert into %s select * from %s where %s.%s >= '%s' and  %s.%s <= '%s'   ;", goalTableName, sourceTableName, sourceTableName, timeField, startTime, sourceTableName, timeField, endTime);
            //同步数据
            tableEnv.executeSql(executeSql);
        }else {
            executeSql=String.format("insert into %s select * from %s ;",goalTableName,sourceTableName,sourceTableName);
            tableEnv.executeSql(executeSql);
        }
    }

    /**
     * 多表MySQL同步mysql
     * @param <T>
     * @param dto
     * @param key
     * @param mysqlDDL
     * @param tableName
     * @param name
     * @return
     */
    public static <T> String SingleTableDDL(T dto, String key, String mysqlDDL, String tableName, String name) {
        ToManyTablesDTO tableDTO = (ToManyTablesDTO) dto;
        // 匹配字段名和数据类型的正则表达式
        Pattern pattern = Pattern.compile("`([^`]+)` ([^ ]+)");
        Matcher matcher = pattern.matcher(mysqlDDL);

        StringBuilder sb = new StringBuilder(String.format("CREATE TABLE %s (\n",tableName));
        // 跳过第一个匹配
        matcher.find();
        // 遍历匹配结果
        while (matcher.find()) {
            String columnName = matcher.group(1);
            String dataType = matcher.group(2);
            // 根据MySQL数据类型转换为对应的Flink CDC数据类型
            String flinkDataType = convertToMysqlDataType(dataType);
            //排除索引项  建表语句类似(KEY `ihsp_id_index` (`ihsp_id`) USING BTREE,)  下述的正则会转换 `ihsp_id_index` (`IHSP_ID`),  不需要索引去执行flinksql
            if (dataType.contains("(`")){
                continue;
            }
            // 构建CDC DDL语句
            sb.append(" ").append("`"+columnName+"`").append(" ").append(flinkDataType).append(",\n");
        }
        String  content="PRIMARY KEY ( %s ) NOT ENFORCED\n";
        //获取主键字段名
        String idName = getPrimaryKey(mysqlDDL);
        if (StringUtils.isEmpty(idName)){
            return "";
        }
        String primaryKey= String.format(content, idName);
        // 添加主键定义
        sb.append(primaryKey);
        sb.append(")");
        String sql= sb.toString();
        if (key.equals("source")){
            return sourceDDL(tableDTO,sql,name);
        }
        if (key.equals("goal")){
            return goalDDL(tableDTO,sql,name);
        }
        return "";
    }

    public static String SingleTableDDL(TableMysqlToMysqlDTO dto, String key, String mysqlDDL, String tableName) {
        // 匹配字段名和数据类型的正则表达式
        Pattern pattern = Pattern.compile("`([^`]+)` ([^ ]+)");
        Matcher matcher = pattern.matcher(mysqlDDL);

        StringBuilder sb = new StringBuilder(String.format("CREATE TABLE %s (\n",tableName));
        // 跳过第一个匹配
        matcher.find();
        // 遍历匹配结果
        while (matcher.find()) {
            String columnName = matcher.group(1);
            String dataType = matcher.group(2);
            // 根据MySQL数据类型转换为对应的Flink CDC数据类型
            String flinkDataType = convertToMysqlDataType(dataType);
            //排除索引项  建表语句类似(KEY `ihsp_id_index` (`ihsp_id`) USING BTREE,)  下述的正则会转换 `ihsp_id_index` (`IHSP_ID`),  不需要索引去执行flinksql
            if (dataType.contains("(`")){
                continue;
            }
            // 构建CDC DDL语句
            sb.append(" ").append("`"+columnName+"`").append(" ").append(flinkDataType).append(",\n");
        }
        String  content="PRIMARY KEY ( %s ) NOT ENFORCED\n";
        //获取主键字段名
        String idName = getPrimaryKey(mysqlDDL);
        String primaryKey= String.format(content, idName);
        // 添加主键定义
        sb.append(primaryKey);
        sb.append(")");
        String sql= sb.toString();
        if (key.equals("source")){
            return sourceDDL(dto,sql);
        }
        if (key.equals("goal")){
            return goalDDL(dto,sql);
        }
        return null;
    }

    /**
     * 转换成flink执行的sql(临时表)
     * @param dto
     * @param mysqlDDL
     * @param tableName(临时表的表名)
     * @return
     */
    public static String convertToCDCDDL(SyncDTO dto, String key, String mysqlDDL, String tableName) {
        // 匹配字段名和数据类型的正则表达式
        Pattern pattern = Pattern.compile("`([^`]+)` ([^ ]+)");
        Matcher matcher = pattern.matcher(mysqlDDL);

        StringBuilder sb = new StringBuilder(String.format("CREATE TABLE %s (\n",tableName));
        // 跳过第一个匹配
        matcher.find();
        // 遍历匹配结果
        while (matcher.find()) {
            String columnName = matcher.group(1);
            String dataType = matcher.group(2);
            // 根据MySQL数据类型转换为对应的Flink CDC数据类型
            String flinkDataType = convertToMysqlDataType(dataType);
            //排除索引项  建表语句类似(KEY `ihsp_id_index` (`ihsp_id`) USING BTREE,)  下述的正则会转换 `ihsp_id_index` (`IHSP_ID`),  不需要索引去执行flinksql
             if (dataType.contains("(`")){
                 continue;
             }
            // 构建CDC DDL语句
            sb.append(" ").append("`"+columnName+"`").append(" ").append(flinkDataType).append(",\n");
        }
        String  content="PRIMARY KEY ( %s ) NOT ENFORCED\n";
        //获取主键字段名
        String idName = getPrimaryKey(mysqlDDL);
        if (StringUtils.isEmpty(idName)){
            return "";
        }
        String primaryKey= String.format(content, idName);
        // 添加主键定义
        sb.append(primaryKey);
        sb.append(")");
        String sql= sb.toString();
        if (key.equals("source")){
            return sourceDDL(dto,sql);
        }
        if (key.equals("goal")){
            return goalDDL(dto,sql);
        }
        return "";
    }

    //转换flink执行的字段类型
    public static String convertToMysqlDataType(String mysqlDataType) {
        //大写转换
        String key= mysqlDataType.toUpperCase();
        if (key.startsWith("TINYINT")){
            return "TINYINT";
        }
        if (key.startsWith("BIGINT")){
            return "BIGINT";
        }
        if (key.startsWith("CHAR")){
            return "STRING";
        }
        if (key.startsWith("LONGTEXT")){
            return "STRING";
        }
        if (key.startsWith("VARCHAR")){
            return "STRING";
        }
        if (key.startsWith("DATETIME")){
            return "TIMESTAMP";
        }
        if (key.startsWith("DATE")){
            return "DATE";
        }
        if (key.startsWith("DECIMAL")){
            return "DECIMAL";
        }
        if (key.startsWith("DOUBLE")){
            return "DOUBLE";
        }
        if (key.startsWith("TIMESTAMP")){
            return "TIMESTAMP";
        }
        if (key.startsWith("INT")){
            return "INT";
        }
        if (key.startsWith("TEXT")){
            return "TEXT";
        }
        return key;
    }

    //获取表的主键名字
    public static String  getPrimaryKey(String content){
        String regex = "PRIMARY KEY \\(`(\\w+)`\\)";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        // 创建匹配器对象
        Matcher matcher = pattern.matcher(content);
        // 查找匹配的子串
        if (matcher.find()) {
            // 提取匹配的子串
            String primaryKey = matcher.group(1);
            return primaryKey;
        } else {
            return "";
        }
    }

    /**
     * 拼接flink 源表的sql数据抽数ddl
     * @param dto
     * @param sql
     * @return
     */
    public static String sourceDDL(SyncDTO dto, String sql) {
        String content = sql+
                " WITH (\n" +
                "   'server-time-zone' = 'Asia/Shanghai',\n" +
                "   'connector' = 'mysql-cdc',\n" +
                "   'hostname' = '%s',\n" +
                "   'port' = '%s',\n" +
                "   'username' = '%s',\n" +
                "   'password' = '%s',\n" +
                "   'database-name' = '%s',\n" +
                "   'table-name' = '%s'\n" +
                ");";
        String sourceDDL = String.format(content, dto.getSourceDatabaseUrl(), dto.getSourceDatabasePort(), dto.getSourceAccountNumber(), dto.getSourcePassWord(), dto.getSourceDatabaseName(), dto.getTableName());
        return sourceDDL;
    }




    /**
     * 拼接flink 目标表的sql数据抽数ddl
     * @param dto
     * @param sql
     * @return
     */
    public static String goalDDL(SyncDTO dto, String sql) {
        String content = sql +
                "WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://%s/%s',\n" +
                "   'driver' = 'com.mysql.cj.jdbc.Driver',\n" +
                "   'username' = '%s',\n" +
                "   'password' = '%s',\n" +
                "   'table-name' = '%s'\n" +
                ");";
        String goalDDL = String.format(content, dto.getGoalDatabaseUrl()+":"+dto.getGoalDatabasePort(), dto.getGoalDatabaseName(), dto.getGoalAccountNumber(), dto.getGoalPassWord(), dto.getTableName());
        return goalDDL;
    }


    /**
     * 拼接flink 源表的sql数据抽数ddl
     * @param dto
     * @param sql
     * @return
     */
    public static String sourceDDL(TableMysqlToMysqlDTO dto, String sql) {
        String content = sql+
                " WITH (\n" +
                "   'connector' = 'mysql-cdc',\n" +
                "   'hostname' = '%s',\n" +
                "   'port' = '%s',\n" +
                "   'username' = '%s',\n" +
                "   'password' = '%s',\n" +
                "   'database-name' = '%s',\n" +
                "   'table-name' = '%s'\n" +
                ");";
        String sourceDDL = String.format(content, dto.getSourceDatabaseUrl(), dto.getSourceDatabasePort(), dto.getSourceAccountNumber(), dto.getSourcePassWord(), dto.getSourceDatabaseName(), dto.getTableName());
        return sourceDDL;
    }

    /**
     * 拼接flink 目标表的sql数据抽数ddl
     * @param dto
     * @param sql
     * @return
     */
    public static String goalDDL(TableMysqlToMysqlDTO dto, String sql) {
        String content = sql +
                "WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://%s/%s',\n" +
                "   'driver' = 'com.mysql.cj.jdbc.Driver',\n" +
                "   'username' = '%s',\n" +
                "   'password' = '%s',\n" +
                "   'table-name' = '%s'\n" +
                ");";
        String goalDDL = String.format(content, dto.getGoalDatabaseUrl()+":"+dto.getGoalDatabasePort(), dto.getGoalDatabaseName(), dto.getGoalAccountNumber(), dto.getGoalPassWord(), dto.getTableName());
        return goalDDL;
    }


    /**
     * 多表抽取 mysql到mysql
     * @param dto
     * @param sql
     * @param name
     * @return
     */
    public static String sourceDDL(ToManyTablesDTO dto, String sql, String name) {
        String content = sql+
                " WITH (\n" +
                "   'connector' = 'mysql-cdc',\n" +
                "   'hostname' = '%s',\n" +
                "   'port' = '%s',\n" +
                "   'username' = '%s',\n" +
                "   'password' = '%s',\n" +
                "   'database-name' = '%s',\n" +
                "   'table-name' = '%s'\n" +
                ");";
        String sourceDDL = String.format(content, dto.getSourceDatabaseUrl(), dto.getSourceDatabasePort(), dto.getSourceAccountNumber(), dto.getSourcePassWord(), dto.getSourceDatabaseName(), name);
        return sourceDDL;
    }

    /**
     * 多表抽取 mysql到mysql
     * @param dto
     * @param sql
     * @param name
     * @return
     */
    public static String goalDDL(ToManyTablesDTO dto, String sql, String name) {
        String content = sql +
                "WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://%s/%s',\n" +
                "   'driver' = 'com.mysql.cj.jdbc.Driver',\n" +
                "   'username' = '%s',\n" +
                "   'password' = '%s',\n" +
                "   'table-name' = '%s'\n" +
                ");";
        String goalDDL = String.format(content, dto.getGoalDatabaseUrl()+":"+dto.getGoalDatabasePort(), dto.getGoalDatabaseName(), dto.getGoalAccountNumber(), dto.getGoalPassWord(), name);
        return goalDDL;
    }


    public static String timeConvert(Date time){
        if (ObjectUtils.isNotEmpty(time)){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = dateFormat.format(time);
            return format;
        }
       return "";
    }
}
