package com.flink.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class TableMessageDTO {
    /**
     * 数据同步的表名
     */
    @ApiModelProperty("数据同步的表名")
    private String tableName;
    /**
     * 时间字段
     */
    @ApiModelProperty("时间字段")
    private String timeField;
    /**
     * 起始时间
     */
    @ApiModelProperty("起始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /**
     * 结尾时间
     */
    @ApiModelProperty("结尾时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    /**
     * 同步的字段名
     */
    @ApiModelProperty("同步的字段名")
    private List<String> syncFields;
}
