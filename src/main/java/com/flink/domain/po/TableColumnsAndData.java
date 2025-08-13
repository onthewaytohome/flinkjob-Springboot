package com.flink.domain.po;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

@Data
public class TableColumnsAndData {
    private List<String> columnNameList;
    private List<LinkedHashMap<String,String>> columnList;
    private Integer columnCount;
}
