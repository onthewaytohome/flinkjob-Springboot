package com.flink.common.util;


import com.flink.domain.dto.Text2sqlTableDto;
import com.flink.domain.po.TableColumnsAndData;
import com.flink.domain.po.TableMetadata;


import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class MysqlMetaData {

    /**
     * 1、连接
     */
    public static R connect(String address, Integer port, String user, String password) {

        String url = "jdbc:mysql://" + address + ":" + port; // 替换为你的MySQL地址和端口

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            return R.success("连接成功");
        } catch (Exception e) {
            return R.error(e.getMessage());
        } finally {
            // 关闭连接
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                return R.error(e.getMessage());
            }
        }
    }

    /**
     * 2、查询所有库名
     * @return
     */
    public static R fetchAllDatabases(String address, Integer port, String user, String password) {
        String url = "jdbc:mysql://" + address + ":" + port; // 替换为你的MySQL地址和端口

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getCatalogs();

            List<String> databaseNames = new ArrayList<>();
            while (resultSet.next()) {
                String dbName = resultSet.getString(1);
                databaseNames.add(dbName);
            }

            resultSet.close();
            conn.close();
            return R.success(databaseNames);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 3、查询所有表名
     * @return
     */
    public static R fetchAllTables(String address, Integer port, String user, String password, String dbName) {

        String url = "jdbc:mysql://" + address + ":" + port; // 替换为你的MySQL地址和端口

        Text2sqlTableDto text2sqlTableDto = new Text2sqlTableDto();
        text2sqlTableDto.setDbName(dbName);
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            DatabaseMetaData metaData = conn.getMetaData();

            ResultSet tables = metaData.getTables(dbName, null, null, new String[]{"TABLE"});
            List<String> tableList = new ArrayList<>();

            while (tables.next()) {
                tableList.add(tables.getString(3));
            }

            conn.close();
            text2sqlTableDto.setTableNameList(tableList);
            return R.success(text2sqlTableDto);

        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }


    /**
     * 4、查询 （表字段 注释） （主键）  （索引）
     */
    public static R fetchAllColumn(String address,Integer port,String user,String password,String dbName,String tableName) {

        String url = "jdbc:mysql://" + address + ":" + port; // 替换为你的MySQL地址和端口

        TableMetadata tableMetadata = new TableMetadata();
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            DatabaseMetaData metaData = conn.getMetaData();

            ResultSet columns = metaData.getColumns(null, dbName, tableName, null);

            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            LinkedHashSet<String> primaryKeySet = new LinkedHashSet<>();
            LinkedHashSet<String> indexSet = new LinkedHashSet<>();

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String remarks = columns.getString("REMARKS");
                map.put(columnName, remarks);
            }

            columns.close();
            //字段名称和注释导入元数据类
            tableMetadata.setColumnAndRemarks(map);

            ResultSet primaryKeys = metaData.getPrimaryKeys(null, dbName, tableName);
            while (primaryKeys.next()) {
                String columnName = primaryKeys.getString("COLUMN_NAME");
                primaryKeySet.add(columnName);
            }
            tableMetadata.setPrimaryKey(primaryKeySet);
            primaryKeys.close();

            ResultSet indexInfo = metaData.getIndexInfo(null, dbName, tableName, false, true);

            while (indexInfo.next()) {
                String columnName = indexInfo.getString("COLUMN_NAME");
                indexSet.add(columnName);
            }
            tableMetadata.setIndexes(indexSet);
            System.out.println(tableMetadata);
            indexInfo.close();
            conn.close();
            return R.success(tableMetadata);

        } catch (Exception e) {
            return R.error(e.getMessage());
        }

    }







    public static R showData(String address,Integer port,String user,String password,String dbName,
                             String tableName,Integer count,Integer page) {
        String url = "jdbc:mysql://" + address + ":" + port+"/"+dbName; // 替换为你的MySQL地址和端口

        String sql = "select * from "+tableName+" limit "+(page-1)*count+","+count;


        String sql2 = "select count(*) from "+tableName; // 查询总条数
        TableColumnsAndData tableColumns2 = new TableColumnsAndData();
        List<LinkedHashMap<String,String>> columnList = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>();

        try {
            Connection conn = DriverManager.getConnection(url, user, password);

            Statement statement = conn.createStatement();
            ResultSet resultSet2 = statement.executeQuery(sql2);
            Integer columnCount  = 0;
            if (resultSet2.next()){
                columnCount = resultSet2.getInt(1);
            }
            tableColumns2.setColumnCount(columnCount);

            conn.close();

            Connection conn2 = DriverManager.getConnection(url, user, password);

            Statement statement2 = conn2.createStatement();
            ResultSet resultSet = statement2.executeQuery(sql);

            while (resultSet.next()) {
                LinkedHashMap<String,String> map = new LinkedHashMap<>();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    String value = resultSet.getString(i);
                    map.put(columnName,value);

                    columnNameList.add(columnName);
                }
                columnList.add(map);
                break;
            }

            while (resultSet.next()) {
                LinkedHashMap<String,String> map = new LinkedHashMap<>();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    String value = resultSet.getString(i);
                    map.put(columnName,value);
                }
                columnList.add(map);
            }

            tableColumns2.setColumnNameList(columnNameList);
            tableColumns2.setColumnList(columnList);

            conn2.close();
            return R.success(tableColumns2);

        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }












    public static Integer getTotal(String address, Integer port, String user, String password, String dbName, String sql, Integer pageNumber, Integer pageSize) {
        String url = "jdbc:mysql://" + address + ":" + port+"/"+dbName;

        try {
            Connection conn = DriverManager.getConnection(url, user, password);

            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            int rowCount = 0;
            while (resultSet.next()) {
                rowCount++;
            }

            return rowCount;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
   