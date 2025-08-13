package com.flink.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class Text2sqlTableDto {
    private String dbName;
    private List<String> tableNameList;
}
