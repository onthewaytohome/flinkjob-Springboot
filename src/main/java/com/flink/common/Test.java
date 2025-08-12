package com.flink.common;

public class Test {

    public static void main(String[] args) {
        String input = "CREATE TABLE `bas_clinictype` (\n" +
                "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                "  `netcode` varchar(30) DEFAULT NULL,\n" +
                "  `father_id` bigint(20) DEFAULT NULL,\n" +
                "  `level` varchar(30) DEFAULT NULL,\n" +
                "  `name` varchar(30) DEFAULT NULL,\n" +
                "  `pincode` varchar(20) DEFAULT NULL,\n" +
                "  `hiscode` varchar(20) DEFAULT NULL,\n" +
                "  `standcode` varchar(20) DEFAULT NULL,\n" +
                "  `ordersn` int(11) DEFAULT NULL,\n" +
                "  `isstop` char(1) DEFAULT NULL,\n" +
                "  `memo` varchar(200) DEFAULT NULL,\n" +
                "  `no_code` varchar(20) DEFAULT NULL COMMENT '国家卫生统计编码',\n" +
                "  PRIMARY KEY (`id`) USING BTREE\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=225 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC";

        // 使用正则表达式删除 "1."、"2." 等序号，但保留小数
        String result = input.replaceAll("\\b\\d+\\.|。", "");

        System.out.println(result);
    }

}
