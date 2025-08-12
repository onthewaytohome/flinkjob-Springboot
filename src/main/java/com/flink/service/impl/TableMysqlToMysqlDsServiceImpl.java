package com.flink.service.impl;


import com.flink.common.util.R;
import com.flink.domain.dto.TableMessageDTO;
import com.flink.domain.dto.TableMysqlToMysqlDTO;
import com.flink.domain.dto.ToManyTablesDTO;
import com.flink.service.TableMysqlToMysqlDsService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.flink.common.util.ConvertUtil.*;

@Service
public class TableMysqlToMysqlDsServiceImpl implements TableMysqlToMysqlDsService {

    @Override
    public R mySqlSync(TableMysqlToMysqlDTO dto) {
        String startTime = timeConvert(dto.getStartTime());
        String endTime = timeConvert(dto.getEndTime());
        String sourceDBUrl = "jdbc:mysql://"+dto.getSourceDatabaseUrl()+":"+dto.getSourceDatabasePort()+"/"+dto.getSourceDatabaseName();
        String goalDBUrl = "jdbc:mysql://"+dto.getGoalDatabaseUrl()+":"+dto.getGoalDatabasePort()+"/"+dto.getGoalDatabaseName();
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
            stmt.close();
            // 连接目标数据库
            targetConn = DriverManager.getConnection(goalDBUrl,dto.getGoalAccountNumber(),dto.getGoalPassWord());

            // 检查目标表是否已经存在
            DatabaseMetaData metaData = targetConn.getMetaData();
            rs = metaData.getTables(dto.getGoalDatabaseName(), null, dto.getTableName(), null);
            boolean tableExists = rs.next();
            if (!tableExists) {
                // 创建与源表相同结构的目标表
                stmt = targetConn.createStatement();
                stmt.executeUpdate(createTableSQL);
                String sourceDDL = SingleTableDDL(dto, "source", createTableSQL, "source_test");
                String goalDDL = SingleTableDDL(dto, "goal", createTableSQL, "goal_test");
                flinkMysqlToMysqlTime(sourceDDL,goalDDL,"source_test","goal_test",dto.getTimeField(),startTime,endTime);
                System.out.println("目标表已成功创建。");
            } else {
                String sourceDDL = SingleTableDDL(dto, "source", createTableSQL, "source_test");
                String goalDDL = SingleTableDDL(dto, "goal" , createTableSQL, "goal_test");
                flinkMysqlToMysqlTime(sourceDDL,goalDDL,"source_test","goal_test",dto.getTimeField(),startTime,endTime);
                System.out.println("目标表已经存在，无需创建。");
            }
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

    @Override
    public R toManyTables(ToManyTablesDTO dto) {
        String sourceDBUrl = "jdbc:mysql://"+dto.getSourceDatabaseUrl()+":"+dto.getSourceDatabasePort()+"/"+dto.getSourceDatabaseName();
        String goalDBUrl = "jdbc:mysql://"+dto.getGoalDatabaseUrl()+":"+dto.getGoalDatabasePort()+"/"+dto.getGoalDatabaseName();
        Connection sourceConn = null;
        Connection targetConn = null;
        try {
            // 连接目标数据库
            targetConn = DriverManager.getConnection(goalDBUrl,dto.getGoalAccountNumber(),dto.getGoalPassWord());
            for (TableMessageDTO tableMessageDTO : dto.getDtoList()) {
                System.out.println("*********************表名:---------------------"+tableMessageDTO.getTableName());
                // 连接源数据库
                sourceConn = DriverManager.getConnection(sourceDBUrl,dto.getSourceAccountNumber(),dto.getSourcePassWord());

                String startTime = timeConvert(tableMessageDTO.getStartTime());
                String endTime = timeConvert(tableMessageDTO.getEndTime());
                // 查询源表结构的 SQL
                String createTableStr = "SHOW CREATE TABLE %s ";
                String query = String.format(createTableStr, tableMessageDTO.getTableName());
                Statement stmt = sourceConn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                // 获取创建表的 SQL 语句
                String createTableSQL = "";

                if (rs.next()) {
                    createTableSQL = rs.getString(2);
                }

                //查询你需要同步表的字段
                if (ObjectUtils.isNotEmpty(tableMessageDTO.getSyncFields())){
                    List<String> columnNames = new ArrayList<>();
                    DatabaseMetaData metaData = sourceConn.getMetaData();
                    //获取所有的字段名称
                    ResultSet columns = metaData.getColumns(null, "%", tableMessageDTO.getTableName(), "%");
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        if (!columnNames.contains(columnName)){
                            columnNames.add(columnName);
                        }
                    }
                    Set<String> set = new HashSet<>(tableMessageDTO.getSyncFields());
                    //过滤的字段名称
                    List<String> result = columnNames.stream()
                            .filter(item -> !set.contains(item))
                            .collect(Collectors.toList());
                    //截取sql语句
                    for (String string : result) {
                        //索引的sql创建语句中的索引删除(筛选表的字段删除)
                        String indexRegex = "(,\\s*)?KEY\\s+`[^`]+`\\s*\\(?\\s*`%s`\\s*\\)?[^,\\n]*(\\n)?";
                        String indexFormat = String.format(indexRegex, string);
                        createTableSQL=  createTableSQL.replaceAll(indexFormat,"");
                        // 定义正则表达式，举例截取字段`id`,从`id`到下一个',的位置全部截取掉(筛选表的字段删除)
                        String fieldRegex =   "`%s`[^\\n]*?,\\s*(?=\\n)";
                        String fieldFormat = String.format(fieldRegex, string);
                        createTableSQL = createTableSQL.replaceAll(fieldFormat,"");
                    }
                }
                // 关闭查询相关的  资源mnb3
                rs.close();
                stmt.close();
                // 检查目标表是否已经存在
                DatabaseMetaData metaData = targetConn.getMetaData();
                rs = metaData.getTables(dto.getGoalDatabaseName(), null, tableMessageDTO.getTableName(), null);
                boolean tableExists = rs.next();
                if (!tableExists) {
                    // 创建与源表相同结构的目标表
                    stmt = targetConn.createStatement();
                    stmt.executeUpdate(createTableSQL);
                    String sourceDDL = SingleTableDDL(dto, "source", createTableSQL, "source_test_"+tableMessageDTO.getTableName(),tableMessageDTO.getTableName());
                    String goalDDL = SingleTableDDL(dto, "goal", createTableSQL, "goal_test_"+tableMessageDTO.getTableName(),tableMessageDTO.getTableName());
                    flinkMysqlToMysqlTime(sourceDDL,goalDDL,"source_test_"+tableMessageDTO.getTableName(),"goal_test_"+tableMessageDTO.getTableName(),tableMessageDTO.getTimeField(),startTime,endTime);
                    System.out.println("目标表已成功创建。");
                } else {
                    String sourceDDL = SingleTableDDL(dto, "source", createTableSQL, "source_test_"+tableMessageDTO.getTableName(),tableMessageDTO.getTableName());
                    String goalDDL = SingleTableDDL(dto, "goal", createTableSQL, "goal_test_"+tableMessageDTO.getTableName(),tableMessageDTO.getTableName());
                    if (StringUtils.isNotEmpty(sourceDDL) && StringUtils.isNotEmpty(goalDDL)){
                        flinkMysqlToMysqlTime(sourceDDL,goalDDL,"source_test_"+tableMessageDTO.getTableName(),"goal_test_"+tableMessageDTO.getTableName(),tableMessageDTO.getTimeField(),startTime,endTime);
                    }
                    System.out.println("目标表已经存在，无需创建。");
                }
            }
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


}
