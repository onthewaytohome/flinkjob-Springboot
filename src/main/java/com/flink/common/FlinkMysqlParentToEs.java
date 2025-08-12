package com.flink.common;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;


public class FlinkMysqlParentToEs {
    public static void main(String[] args) throws Exception  {

        StreamExecutionEnvironment env = null;
        try {
            // 创建流处理环境
            env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.enableCheckpointing(30000L).setParallelism(2);
            EnvironmentSettings settings = EnvironmentSettings
                    .newInstance()
                    .inStreamingMode()
                    .build();
            StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

            // 创建输入和输出表
            tableEnv.executeSql(sourceDDL());
            tableEnv.executeSql(sinkDDL());

            // 从 source_test 表中读取数据
            Table sourceTable = tableEnv.from("source_test");



            // 将结果写入 sink_test 表中
            TableResult tableResult = tableEnv.executeSql("insert into sink_test (id, name, level, ficaClass)" +
                    "select id+300000 as id,name,level,ficaClass from "+sourceTable );

            // 执行作业并等待完成
            tableResult.await();
            System.out.println("Job finished.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (env != null) {
                env.close();
            }
        }

    }


    public static String sourceDDL() {
        String sourceDDL = "CREATE TABLE source_test (" +
                "   id INT NOT NULL," +
                "  ficaClass  STRING," +
                "   name  STRING," +
                "   level STRING," +
                "  PRIMARY KEY (id) NOT ENFORCED" +
                ") WITH (" +
                "   'connector' = 'mysql-cdc'," +
                "   'hostname' = '172.18.0.195'," +
                "   'port' = '3306'," +
                "   'username' = 'root'," +
                "   'password' = '123456'," +
                "   'database-name' = 'big_data'," +
                "   'table-name' = 'women_children_first'" +
                ");";
        return sourceDDL;
    }

    public static String sinkDDL() {
        String sinkDDL = "CREATE TABLE sink_test (" +
                "  id INT," +
                "  name STRING," +
                "  patientId STRING," +
                "  patientName STRING," +
                "  content STRING," +
                "  ficaClass STRING," +
                "   level STRING," +
                "  PRIMARY KEY (id) NOT ENFORCED" +
                ") WITH (" +
                "   'connector' = 'elasticsearch-7'," +
                "    'hosts' = 'http://172.18.0.195:9200'," +
                "   'index' = 'disease_guide20000'" +
                ");";
        return sinkDDL;
    }
}
