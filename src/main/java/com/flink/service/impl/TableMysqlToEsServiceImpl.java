package com.flink.service.impl;

import com.flink.common.util.R;
import com.flink.domain.dto.TableMysqlToEsDTO;
import com.flink.service.TableMysqlToEsService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.flink.common.util.ConvertUtil.*;
import static com.flink.common.util.esConvertUtil.SingleTableToesDDL;

@Service
public class TableMysqlToEsServiceImpl implements TableMysqlToEsService {

    @Override
    public R singleTableToEs(TableMysqlToEsDTO dto) {
        String startTime = timeConvert(dto.getStartTime());
        String endTime = timeConvert(dto.getEndTime());
        String sourceDBUrl = "jdbc:mysql://"+dto.getSourceDatabaseUrl()+":"+dto.getSourceDatabasePort()+"/"+dto.getSourceDatabaseName();
        Connection sourceConn = null;
        Connection targetConn = null;
        try {
            // 连接源数据库
            sourceConn = DriverManager.getConnection(sourceDBUrl,dto.getSourceAccountNumber(),dto.getSourcePassWord());

            // 查询源表结构的 SQL
            String createTableStr = "SHOW CREATE TABLE %s ";
            String query = String.format(createTableStr, dto.getTableName());
            Statement stmt = sourceConn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // 获取创建表的 SQL 语句
            String createTableSQL = "";

            if (rs.next()) {
                createTableSQL = rs.getString(2);
            }

            //查询你需要同步表的字段
            if (ObjectUtils.isNotEmpty(dto.getSyncFields())){
                List<String> columnNames = new ArrayList<>();
                DatabaseMetaData metaData = sourceConn.getMetaData();
                //获取所有的字段名称
                ResultSet columns = metaData.getColumns(null, "%", dto.getTableName(), "%");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    columnNames.add(columnName);
                }
                Set<String> set = new HashSet<>(dto.getSyncFields());
                //过滤的字段名称
                List<String> result = columnNames.stream()
                        .filter(item -> !set.contains(item))
                        .collect(Collectors.toList());
                //截取sql语句
                for (String string : result) {
                    // 定义正则表达式，匹配从`uuid`开始，直到逗号和后面的空白字符
                    String regex = "`%s`.+?',\\s*";
                    String format = String.format(regex, string);
                    Pattern pattern = Pattern.compile(format, Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(createTableSQL);
                    // 替换匹配到的内容为空字符串
                    createTableSQL = matcher.replaceAll("");
                }
            }
            // 关闭查询相关的资源mnb3
            rs.close();
            stmt .close();
            String sourceDDL = SingleTableToesDDL(dto, "source", createTableSQL, "source_test");
            String goalDDL = SingleTableToesDDL(dto,"goal", createTableSQL, "goal_test");
            flinkMysqlToEsTime(sourceDDL,goalDDL,"source_test","goal_test",dto.getTimeField(),startTime,endTime);
            System.out.println("目标表已成功创建。");
            return R.success("创建成功");
        } catch (SQLException e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        } finally {
            // 关闭连接
            try {
                if (sourceConn != null) {
                    sourceConn.close();
                }
                if (targetConn != null) {
                    targetConn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return R.error(e.getMessage());
            }

        }
    }





    public static void  flinkMysqlToEsTime(String sourceDDL,String goalDDL,String sourceTableName,String goalTableName,String timeField,String startTime,String endTime){
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
        if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)){
            //同步数据
            tableEnv.executeSql(String.format("insert into %s select * from %s where %s.%s >= '%s' and  %s.%s <= '%s';",goalTableName,sourceTableName,sourceTableName,timeField,startTime,sourceTableName,timeField,endTime));
        }else {
            tableEnv.executeSql(String.format("insert into %s select * from %s ;",goalTableName,sourceTableName,sourceTableName));
        }
    }
}
