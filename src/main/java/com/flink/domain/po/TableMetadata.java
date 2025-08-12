package com.flink.domain.po;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

@Data
public class TableMetadata {
    private LinkedHashMap<String,String> columnAndRemarks;
    private LinkedHashSet<String> primaryKey;
    private LinkedHashSet<String> indexes;


}