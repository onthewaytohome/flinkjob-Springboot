package com.flink.common.util;


import com.flink.domain.dto.TableMysqlToEsDTO;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.flink.common.util.ConvertUtil.convertToCDCDataType;
import static com.flink.common.util.ConvertUtil.getPrimaryKey;

public class esConvertUtil {

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
            String flinkDataType = convertToCDCDataType(dataType);
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
