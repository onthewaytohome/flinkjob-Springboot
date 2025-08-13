package com.flink.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



@Data
@ApiModel(value = "ES连接入参")
public class EsToolDto {
    @ApiModelProperty(value = "地址", required = true)
    private String address;

    @ApiModelProperty(value = "端口", required = true)
    private String port;

    // 公共的静态工厂方法，用于生成EsToolDto实例并返回连接字符串
    public static String createEsUrl(String address, String port) {
        return "http://" + address + ":" + port;
    }

}