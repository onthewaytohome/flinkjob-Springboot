package com.flink.common.util;


import com.flink.domain.dto.TableMysqlToEsDTO;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class EsConvertUtil {

    public static String SingleTableToesDDL(TableMysqlToEsDTO dto, String key, String mysqlDDL, String tableName) {
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
            String flinkDataType = convertToEsDataType(dataType);
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


    //转换flink执行的字段类型
    public static String convertToEsDataType(String mysqlDataType) {
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
            return "STRING";
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
    public static String sourceDDL(TableMysqlToEsDTO dto, String sql) {
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
    public static String goalDDL(TableMysqlToEsDTO dto, String sql) {
        String content = sql +
                "WITH (\n" +
                "   'connector' = '%s',\n" +
                "   'hosts' = '%s',\n" +
                "   'index' = '%s'\n" +
                ");";
        String goalDDL = String.format(content,dto.getEsConVersion(),dto.getEsAddress(),dto.getIndexName());
        return goalDDL;
    }
}
