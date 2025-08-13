package com.flink.common;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class FlinkMysqlChildToEs {
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
            tableEnv.executeSql(sourceDDL1());
            tableEnv.executeSql(sourceDDL2());
            tableEnv.executeSql(sinkDDL());


            // 将结果写入 sink_test 表中
            TableResult tableResult = tableEnv.executeSql("insert into sink_test (id,name,patientId,patientName,content,ficaClass,level)" +
                    "SELECT wc.id+300025 AS id,wc.typical_functions AS name,CAST(wcf.id + 300000 AS STRING) AS patientId,wcf.name  AS patientName,wc.full_detail3 AS content,wc.one_category AS ficaClass,'2' AS level from women_children wc,women_children_first wcf where wc.two_category =wcf.name" );

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


    public static String sourceDDL1() {
        String sourceDDL = "CREATE TABLE women_children_first (" +
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


    public static String sourceDDL2() {
        String sourceDDL = "CREATE TABLE women_children (" +
                "   id INT NOT NULL," +
                "  one_category  STRING," +
                "  two_category  STRING," +
                "   typical_functions STRING," +
                "   full_detail3 STRING," +
                "  PRIMARY KEY (id) NOT ENFORCED" +
                ") WITH (" +
                "   'connector' = 'mysql-cdc'," +
                "   'hostname' = '172.18.0.195'," +
                "   'port' = '3306'," +
                "   'username' = 'root'," +
                "   'password' = '123456'," +
                "   'database-name' = 'big_data'," +
                "   'table-name' = 'women_children'" +
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
