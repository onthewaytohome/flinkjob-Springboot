package com.flink.service.impl;

import com.flink.domain.dto.SyncDTO;
import com.flink.service.TablesMysqlToMysqlDsService;
import org.springframework.stereotype.Service;

import java.sql.*;

import static com.flink.common.util.ConvertUtil.convertToCDCDDL;
import static com.flink.common.util.ConvertUtil.flinkMysqlToMysql;

@Service
public class TablesMysqlToMysqlDsServiceImpl implements TablesMysqlToMysqlDsService {
    @Override
    public void mySqSync(SyncDTO dto) {
        //根据多个表名实时同步数据
        for (int i = 0; i < dto.getTableNames().size(); i++) {
            String sourceDBUrl = "jdbc:mysql://"+dto.getSourceDatabaseUrl()+":"+dto.getSourceDatabasePort()+"/"+dto.getSourceDatabaseName();
            String goalDBUrl = "jdbc:mysql://"+dto.getGoalDatabaseUrl()+":"+dto.getGoalDatabasePort()+"/"+dto.getGoalDatabaseName();
            Connection sourceConn = null;
            Connection targetConn = null;
            try {
                dto.setTableName(dto.getTableNames().get(i));
                System.out.println(dto.getTableName());
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
                // 关闭查询相关的资源mnb3
                rs.close();
                stmt.close();
                // 连接目标数据库
                targetConn = DriverManager.getConnection(goalDBUrl,dto.getGoalAccountNumber(),dto.getGoalPassWord());

                // 检查目标表是否已经存在
                DatabaseMetaData metaData = targetConn.getMetaData();
                //检查该数据库下是否有该表
                rs = metaData.getTables(dto.getGoalDatabaseName(), null, dto.getTableName(), null);
                boolean tableExists = rs.next();
                if (!tableExists) {
                    // 创建与源表相同结构的目标表
                    stmt = targetConn.createStatement();
                    stmt.executeUpdate(createTableSQL);
                    String sourceDDL = convertToCDCDDL(dto, "source", createTableSQL, "source_test"+i);
                    String goalDDL = convertToCDCDDL(dto, "goal", createTableSQL, "goal_test"+i);
                    flinkMysqlToMysql(sourceDDL,goalDDL,"source_test"+i,"goal_test"+i);
                    System.out.println("目标表已成功创建。");
                } else {
                    String sourceDDL = convertToCDCDDL(dto, "source", createTableSQL, "source_test"+i);
                    String goalDDL = convertToCDCDDL(dto, "goal", createTableSQL, "goal_test"+i);
                    flinkMysqlToMysql(sourceDDL,goalDDL,"source_test"+i,"goal_test"+i);
                    System.out.println("目标表已经存在，无需创建。");
                }
            } catch (SQLException e) {
                e.printStackTrace();
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
                }

            }
        }

    }

}
